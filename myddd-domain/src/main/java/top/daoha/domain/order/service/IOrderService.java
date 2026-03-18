package top.daoha.domain.order.service;

import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;

import java.util.List;

/**
 * @ClassName : IOrderService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  12:02
 */

public interface IOrderService {

    PayOrderEntity createOrder(ShopCartEntity shopCartEntity)throws Exception;

    void changeOrderPaySuccess(String orderId);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeOutCloseOrderList();

    boolean changeOrderClose(String orderId) ;

}
