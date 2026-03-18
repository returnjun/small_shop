package top.daoha.domain.order.model.aggregate;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.RandomStringUtils;
import top.daoha.domain.order.model.entity.OrderEntity;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.domain.order.model.valobj.OrderStatusVO;

import java.util.Date;

/**
 * @ClassName : CreateOrderAggregate
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  21:52
 */
@Builder
@Data
public class CreateOrderAggregate {

    private String userId;

    private ProductEntity productEntity;

    private OrderEntity orderEntity;

    public static OrderEntity buildOrderEntity(String productId,String productName){
        return OrderEntity.builder()
                .productId(productId)
                .productName(productName)
                .orderStatusVO(OrderStatusVO.CREATE)
                .orderId(RandomStringUtils.randomNumeric(14))
                .orderTime(new Date())
                .build();
    }
}
