package top.daoha.infrastructure.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementMarketPayOrderResponseDTO {

    /**  用户id */
    private String userId;
    /**  团队id */
    private String teamId;
    /**  活动id */
    private Long activityId;
    /**  外部订单编号 */
    private String outTradeNo;
}
