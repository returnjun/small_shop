package top.daoha.infrastructure.adapter.repository;

import com.google.common.eventbus.EventBus;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import top.daoha.domain.order.adapter.event.PaySuccessMessageEvent;
import top.daoha.domain.order.adapter.repository.IOrderRepository;
import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.infrastructure.dao.IOrderDao;
import top.daoha.infrastructure.dao.po.PayOrder;
import top.daoha.types.common.Constants;
import top.daoha.types.event.BaseEvent;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName : OrderRepository
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  9:09
 */
@Repository
public class OrderRepository implements IOrderRepository {
    @Resource
    private IOrderDao iOrderDao;

    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

    @Resource
    private EventBus eventBus;

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
                .build();
    }

    @Override
    public void updatePayInfo(PayOrderEntity payOrderEntity) {
        PayOrder order = new PayOrder();
        order.setUserId(payOrderEntity.getUserId());
        order.setOrderId(payOrderEntity.getOrderId());
        order.setPayUrl(payOrderEntity.getPayUrl());
        order.setStatus(payOrderEntity.getOrderStatus().getCode());
        iOrderDao.updateOrderPayInfo(order);
    }

    @Override
    public void changeOrderPaySuccess(String orderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setOrderId(orderId);
        payOrder.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        iOrderDao.changeOrderPaySuccess(payOrder);

        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> paySuccessMessageEventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder().tradeNo(orderId).build());
        PaySuccessMessageEvent.PaySuccessMessage data = paySuccessMessageEventMessage.getData();
        eventBus.post(data);
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
}
