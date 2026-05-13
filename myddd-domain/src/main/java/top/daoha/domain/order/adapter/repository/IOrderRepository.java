package top.daoha.domain.order.adapter.repository;


import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.UserStatisticVO;

import java.util.Date;
import java.util.List;

public interface IOrderRepository {

    void doSaveOrder(CreateOrderAggregate build);

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    void updatePayInfo(PayOrderEntity payOrderEntity);

    void changeOrderPaySuccess(String orderId, Date payTime);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeOutCloseOrderList();

    boolean changeOrderClose(String orderId);

    OrderEntity queryOrderByOrderId(String orderId);

    void changeMarketOrderPaySuccess(String orderId);

    void changeOrderMarketSettlement(List<String> outTradeNoList);

    List<OrderEntity> queryUserOrderList(String userId, Long lastId, Integer pageSize, List<String> dbStatusList);

    UserStatisticVO queryUserOrderStatistics(String userId);

    OrderEntity queryOrderByUserIdAndOrderId(String userId, String orderId);

    boolean refundOrder(String userId, String orderId);

    boolean refundMarketOrder(String userId, String orderId);
}
