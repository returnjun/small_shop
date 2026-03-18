package top.daoha.infrastructure.adapter.repository;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
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

import javax.annotation.Resource;
import java.util.Date;

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

    @Override
    public void doSaveOrder(CreateOrderAggregate build) {
        String userId=build.getUserId();
        ProductEntity productEntity=build.getProductEntity();
        OrderEntity orderEntity=build.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(order.getTotalAmount());
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
        if(null == order) return null;

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
}
