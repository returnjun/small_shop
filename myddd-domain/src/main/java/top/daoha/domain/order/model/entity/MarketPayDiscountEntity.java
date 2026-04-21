package top.daoha.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketPayDiscountEntity {

    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 营销金额；优惠金额 */
    private BigDecimal marketDeductionAmount;
    /** 支付金额 */
    private BigDecimal payPrice;

}
