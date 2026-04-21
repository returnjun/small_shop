package top.daoha.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.types.common.Constants;

import java.math.BigDecimal;

/**
 * @ClassName : PayOrderEntity
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  11:59
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayOrderEntity {
    private String userId;
    private String orderId;
    private String payUrl;
    private OrderStatusVO orderStatus;

    /** 营销类型：0无营销、1拼团营销 */
    private Integer marketType;
    /** 营销金额；优惠金额 */
    private BigDecimal marketDeductionAmount;
    /** 支付金额 */
    private BigDecimal payAmount;
}
