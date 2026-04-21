package top.daoha.infrastructure.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockMarketPayOrderRequestDTO {
    /** 用户ID */
    private String userId;
    /** 拼单组队ID */
    private String teamId;
    /** 商品id */
    private String goodsId;
    /** 活动ID */
    private Long activityId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;
    /** 回调通知（HTTP 方式回调，地址不可为空） */
    private String notifyUrl;

}
