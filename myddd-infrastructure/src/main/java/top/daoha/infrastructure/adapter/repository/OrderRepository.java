package top.daoha.infrastructure.adapter.repository;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import top.daoha.domain.order.adapter.event.PaySuccessMessageEvent;
import top.daoha.domain.order.adapter.repository.IOrderRepository;
import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.MarketTypeVO;
import top.daoha.domain.order.model.valobj.OrderCount;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.domain.order.model.valobj.UserStatisticVO;
import top.daoha.infrastructure.dao.IOrderDao;
import top.daoha.infrastructure.dao.po.PayOrder;
import top.daoha.infrastructure.event.EventPublisher;
import top.daoha.types.common.Constants;
import top.daoha.types.event.BaseEvent;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName : OrderRepository
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  9:09
 */
@Slf4j
@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOrderDao iOrderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventBus eventBus;

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public void doSaveOrder(CreateOrderAggregate build) {
        String userId = build.getUserId();
        ProductEntity productEntity = build.getProductEntity();
        OrderEntity orderEntity = build.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(productEntity.getPrice());
        order.setStatus(orderEntity.getOrderStatusVO().getCode());
        order.setMarketType(MarketTypeVO.NO_MARKET.getCode());
        order.setPayAmount(productEntity.getPrice());
        order.setMarketDeductionAmount(BigDecimal.ZERO);

        iOrderDao.insert(order);
    }

    @Override
    public OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity) {
        //1 封装参数
        PayOrder orderReq = new PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId());
        orderReq.setProductId(shopCartEntity.getProductId());

        //2查询订单
        PayOrder order = iOrderDao.queryUnPayOrder(orderReq);
        if (null == order) return null;

        //3 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .marketType(order.getMarketType())
                .marketDeductionAmount(order.getMarketDeductionAmount())
                .payAmount(order.getPayAmount())
                .build();
    }

    @Override
    public void updatePayInfo(PayOrderEntity payOrderEntity) {
        PayOrder order = new PayOrder();
        order.setUserId(payOrderEntity.getUserId());
        order.setOrderId(payOrderEntity.getOrderId());
        order.setPayUrl(payOrderEntity.getPayUrl());
        order.setStatus(payOrderEntity.getOrderStatus().getCode());
        order.setMarketDeductionAmount(payOrderEntity.getMarketDeductionAmount());
        order.setPayAmount(payOrderEntity.getPayAmount());
        order.setMarketType(payOrderEntity.getMarketType());
        iOrderDao.updateOrderPayInfo(order);
    }

    @Override
    public void changeOrderPaySuccess(String orderId,Date payTime) {
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        iOrderDao.changeOrderPaySuccess(payOrder);

        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent
                .buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder()
                                                                            .tradeNo(orderId)
                                                                            .build());
        PaySuccessMessageEvent.PaySuccessMessage data = paySuccessMessageEventMessage.getData();

        //原来的旧版eventBus发消息方式
        //eventBus.post(JSON.toJSONString(data));

        eventPublisher.publish(paySuccessMessageEvent.topic(),JSON.toJSONString(data));
    }

    @Override
    public List<String> queryNoPayNotifyOrder() {
         return iOrderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeOutCloseOrderList() {
        return iOrderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderClose(String orderId) {
        return iOrderDao.changeOrderClose(orderId);
    }

    @Override
    public OrderEntity queryOrderByOrderId(String orderId) {
        PayOrder payOrder = iOrderDao.queryOrderByOrderId(orderId);
        if(null == payOrder) return null;

        return OrderEntity.builder()
                .userId(payOrder.getUserId())
                .productId(payOrder.getProductId())
                .productName(payOrder.getProductName())
                .orderId(payOrder.getOrderId())
                .orderStatusVO(OrderStatusVO.valueOf(payOrder.getStatus()))
                .orderTime(payOrder.getOrderTime())
                .totalAmount(payOrder.getTotalAmount())
                .payUrl(payOrder.getPayUrl())
                .marketType(payOrder.getMarketType())
                .marketDeductionAmount(payOrder.getMarketDeductionAmount())
                .payAmount(payOrder.getPayAmount())
                .build();
    }

    @Override
    public void changeMarketOrderPaySuccess(String orderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        iOrderDao.changeOrderPaySuccess(payOrder);
    }

    @Override
    public void changeOrderMarketSettlement(List<String> outTradeNoList) {
        iOrderDao.changeOrderMarketSettlement(outTradeNoList);

        outTradeNoList.forEach(outTradeNo ->{
            BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent
                    .buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder()
                            .tradeNo(outTradeNo)
                            .build());
            PaySuccessMessageEvent.PaySuccessMessage data = paySuccessMessageEventMessage.getData();
//            eventBus.post(JSON.toJSONString(data));
            eventPublisher.publish(paySuccessMessageEvent.topic(),JSON.toJSONString(data));
        });
    }

    @Override
    public List<OrderEntity> queryUserOrderList(String userId, Long lastId, Integer pageSize, List<String> dbStatusList) {

        List<PayOrder> payOrderList = iOrderDao.queryUserOrderList(userId, lastId, pageSize,dbStatusList);
        if (null == payOrderList || payOrderList.isEmpty()) {
            return new ArrayList<>();
        }
        return payOrderList.stream().map(payOrder -> OrderEntity.builder()
                .id(payOrder.getId())
                .userId(payOrder.getUserId())
                .productId(payOrder.getProductId())
                .productName(payOrder.getProductName())
                .orderId(payOrder.getOrderId())
                .orderTime(payOrder.getOrderTime())
                .totalAmount(payOrder.getTotalAmount())
                .orderStatusVO(OrderStatusVO.valueOf(payOrder.getStatus()))
                .payUrl(payOrder.getPayUrl())
                .payTime(payOrder.getPayTime())
                .marketType(payOrder.getMarketType())
                .marketDeductionAmount(payOrder.getMarketDeductionAmount())
                .payAmount(payOrder.getPayAmount())
                .build()).collect(Collectors.toList());
    }

    @Override
    public UserStatisticVO queryUserOrderStatistics(String userId) {

        List<OrderCount> rawData = iOrderDao.queryUserOrderStatistics(userId);
        log.info("---------------\n让我们来看看能不能查到数据rawData: {}", rawData);
        // 2. 将 List 转换为 Map，方便后续取值：Key 是状态码，Value 是数量
        Map<String, Integer> countMap = rawData.stream()
                .collect(Collectors.toMap(OrderCount::getStatus, OrderCount::getCount));

        // 3. 动态组装前端需要的 VO
        UserStatisticVO vo = new UserStatisticVO();
        // 待付款 = CREATE + PAY_WAIT
        vo.setWaitingPayCount(
                countMap.getOrDefault(OrderStatusVO.CREATE.getCode(), 0) +
                        countMap.getOrDefault(OrderStatusVO.PAY_WAIT.getCode(), 0)
        );

        // 待发货/已支付 = PAY_SUCCESS
        vo.setWaitingDeliveryCount(countMap.getOrDefault(OrderStatusVO.PAY_SUCCESS.getCode(), 0));

        // 交易完成 = DEAL_DONE
        vo.setWaitingReceiveCount(countMap.getOrDefault(OrderStatusVO.DEAL_DONE.getCode(), 0)+
                countMap.getOrDefault(OrderStatusVO.CLOSE.getCode(), 0));
        vo.sum();
        return vo;
    }

    @Override
    public OrderEntity queryOrderByUserIdAndOrderId(String userId, String orderId) {
        PayOrder payOrder = iOrderDao.queryOrderByUserIdAndOrderId(userId, orderId);
        if (null == payOrder) return null;

        return OrderEntity.builder()
                .id(payOrder.getId())
                .userId(payOrder.getUserId())
                .productId(payOrder.getProductId())
                .productName(payOrder.getProductName())
                .orderId(payOrder.getOrderId())
                .orderTime(payOrder.getOrderTime())
                .totalAmount(payOrder.getTotalAmount())
                .orderStatusVO(OrderStatusVO.valueOf(payOrder.getStatus()))
                .payUrl(payOrder.getPayUrl())
                .payTime(payOrder.getPayTime())
                .marketType(payOrder.getMarketType())
                .marketDeductionAmount(payOrder.getMarketDeductionAmount())
                .payAmount(payOrder.getPayAmount())
                .build();
    }

    @Override
    public boolean refundOrder(String userId, String orderId) {
        return iOrderDao.refundOrder(userId, orderId);
    }

    @Override
    public boolean refundMarketOrder(String userId, String orderId) {
        return iOrderDao.refundMarketOrder(userId, orderId);
    }

}
