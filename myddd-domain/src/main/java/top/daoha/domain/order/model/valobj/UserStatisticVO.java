package top.daoha.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticVO {
    /** 用户订单总量 */
    private Integer userPayCount;
    /** 待支付订单数量 */
    private Integer waitingPayCount;
    /** 待发货订单数量 */
    private Integer waitingDeliveryCount;
    /** 待收货订单数量 */
    private Integer waitingReceiveCount;

    public void sum() {
        this.userPayCount = this.waitingPayCount + this.waitingDeliveryCount+this.waitingReceiveCount;
    }
}
