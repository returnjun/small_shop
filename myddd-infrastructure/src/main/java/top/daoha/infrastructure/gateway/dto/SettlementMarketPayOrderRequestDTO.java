package top.daoha.infrastructure.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementMarketPayOrderRequestDTO {
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /**  用户id */
    private String userId;
    /**  外部订单编号 */
    private String outTradeNo;
    /** 外部交易时间 */
    private Date outTradeTime;
}
