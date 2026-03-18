package top.daoha.domain.order.adapter.repository;


import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;

public interface IOrderRepository {

    void doSaveOrder(CreateOrderAggregate build);

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);
}
