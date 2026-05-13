package top.daoha.domain.order.service;

import com.alipay.api.AlipayApiException;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.UserStatisticVO;

import java.util.Date;
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

    void changeOrderPaySuccess(String orderId, Date payTime);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeOutCloseOrderList();

    boolean changeOrderClose(String orderId) ;

    void changeOrderMarketSettlement(List<String> outTradeNoList);

    List<OrderEntity> queryUserOrderList(String userId, Long lastId, Integer pageSize,Integer orderStatus);

    UserStatisticVO queryUserOrderStatistics(String userId);

    boolean refundMarketOrder(String userId, String orderId);

    boolean refundOrder(String userId, String orderId) throws AlipayApiException;
}
