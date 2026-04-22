package top.daoha.domain.order.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.adapter.repository.IOrderRepository;
import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.MarketPayDiscountEntity;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.MarketTypeVO;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.types.common.Constants;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName : OrderService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  22:57
 */
@Slf4j
@Service
public class OrderService extends AbstractOrderService {

    @Value("${alipay.notify_url}")
    private String notify_url;
    @Value("${alipay.return_url}")
    private String return_url;

    @Resource
    private AlipayClient alipayClient;   //核心的执行器,把请求发往支付宝服务器


    public OrderService(IOrderRepository iOrderRepository, IProductPort iProductPort) {
        super(iOrderRepository, iProductPort);
    }


    @Override
    protected MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId) {
        return iProductPort.lockMarketPayOrder(userId, teamId, activityId, productId, orderId);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException {
        BigDecimal payAmount = null == marketPayDiscountEntity ? totalAmount : marketPayDiscountEntity.getPayPrice();
        //核心作用向支付宝发起预支付请求，并获取支付表单
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 支付宝 SDK 提供的电脑网站支付专用请求类
        request.setNotifyUrl(notify_url);
        request.setReturnUrl(return_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);  // 我们自己生成的订单编号
        bizContent.put("total_amount", payAmount.toString()); // 订单的总金额
        bizContent.put("subject", productName);   // 支付的名称
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");  // 固定配置
        request.setBizContent(bizContent.toString());

        String form = alipayClient.pageExecute(request).getBody();
        //AlipayClient 会自动帮你完成签名、组装参数、发送 HTTP 请求。
        log.info("测试结果\r\n\n把我复制到 index.html ->：{}", form);

        PayOrderEntity payOrderEntity = new PayOrderEntity();
        payOrderEntity.setOrderId(orderId);
        payOrderEntity.setPayUrl(form);
        payOrderEntity.setOrderStatus(OrderStatusVO.PAY_WAIT);
        payOrderEntity.setPayAmount(payAmount);
        payOrderEntity.setMarketDeductionAmount(null == marketPayDiscountEntity ? BigDecimal.ZERO : marketPayDiscountEntity.getMarketDeductionAmount());
        payOrderEntity.setMarketType(null == marketPayDiscountEntity ? MarketTypeVO.NO_MARKET.getCode() : MarketTypeVO.GROUP_BUY_MARKET.getCode());

        iOrderRepository.updatePayInfo(payOrderEntity);

        return payOrderEntity;
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        return doPrepayOrder(productId, productName, orderId, totalAmount, null);
    }


    @Override
    protected void doSaveOrder(CreateOrderAggregate build) {
        iOrderRepository.doSaveOrder(build);
    }

    @Override
    public void changeOrderPaySuccess(String orderId, Date payTime) {
        //支付成功后会走到这一步
        //查看一下是否已经拼团结束能不能直接发货
        OrderEntity orderEntity = iOrderRepository.queryOrderByOrderId(orderId);

        if(null == orderEntity) return;
        //下面要考虑是否走了拼团，因为不走拼团可以直接完成订单，走拼团的话需要看看拼团内情况
        if(MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(orderEntity.getMarketType())){
            //这是走营销的情况
            iOrderRepository.changeMarketOrderPaySuccess(orderId);

            //正式向拼团系统发布支付完成动作
            iProductPort.settlementMarketPayOrder(orderEntity.getUserId(), orderId, payTime);
            // 注意；在公司中，发起结算的http/rpc调用可能会失败，这个时候还会有增加job任务补偿。条件为，检查一笔走了拼团的订单，超过n分钟后，仍然没有做拼团结算状态变更。
            // 我们这里失败了，会抛异常，借助支付宝回调/job来重试。你可以单独实现一个独立的job来处理。
        }else {
            //这是不走营销的情况
            iOrderRepository.changeOrderPaySuccess(orderId,payTime);
        }
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
        return iOrderRepository.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeOutCloseOrderList() {
        return iOrderRepository.queryTimeOutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return iOrderRepository.changeOrderClose(orderId);
    }

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        iOrderRepository.changeOrderMarketSettlement(outTradeNoList);
    }
}
