package top.daoha.domain.order.service;

import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;

/**
 * @ClassName : IOrderService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  12:02
 */

public interface IOrderService {
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity)throws Exception;

}
