package top.daoha.domain.order.adapter.repository;


import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;

import java.util.List;

public interface IOrderRepository {

    void doSaveOrder(CreateOrderAggregate build);

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    void updatePayInfo(PayOrderEntity payOrderEntity);

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeOutCloseOrderList();

    boolean changeOrderClose(String orderId);

}
