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
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.types.common.Constants;

import java.math.BigDecimal;

/**
 * @ClassName : OrderService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  22:57
 */
@Slf4j
@Service
public  class OrderService extends AbstractOrderService{

    @Value("${alipay.notify_url}")
    private String notify_url;
    @Value("${alipay.return_url}")
    private String return_url;

    private AlipayClient alipayClient;   //核心的执行器,把请求发往支付宝服务器


    public OrderService(IOrderRepository iOrderRepository, IProductPort iProductPort) {
        super(iOrderRepository, iProductPort);
    }

    @Override
    protected PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException {
        //核心作用向支付宝发起预支付请求，并获取支付表单
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();  // 支付宝 SDK 提供的电脑网站支付专用请求类
        request.setNotifyUrl(notify_url);
        request.setReturnUrl(return_url);

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orderId);  // 我们自己生成的订单编号
        bizContent.put("total_amount", totalAmount.toString()); // 订单的总金额
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

//        orderDao.updateOrderPayInfo(payOrder);
        iOrderRepository.updatePayInfo(payOrderEntity);
        return payOrderEntity;
    }


    @Override
    protected void doSaveOrder(CreateOrderAggregate build) {
        iOrderRepository.doSaveOrder(build);
    }
}
