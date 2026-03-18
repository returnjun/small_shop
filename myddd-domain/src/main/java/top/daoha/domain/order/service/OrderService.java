package top.daoha.domain.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.adapter.repository.IOrderRepository;
import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;

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


    public OrderService(IOrderRepository iOrderRepository, IProductPort iProductPort) {
        super(iOrderRepository, iProductPort);
    }

    @Override
    protected OrderEntity doPrepayOrder(String productName, String orderId, BigDecimal totalAmount) {
        return null;
    }

    @Override
    protected void doSaveOrder(CreateOrderAggregate build) {
        iOrderRepository.doSaveOrder(build);
    }
}
