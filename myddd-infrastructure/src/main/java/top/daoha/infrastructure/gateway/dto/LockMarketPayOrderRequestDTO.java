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
    /** 回调通知（HTTP 或者 MQ） */
    private NotifyConfigVO notifyConfigVO;


    public void setNotifyUrl(String url) {
        NotifyConfigVO notifyConfigVO = new NotifyConfigVO();
        notifyConfigVO.setNotifyType("HTTP");
        notifyConfigVO.setNotifyUrl(url);
        this.notifyConfigVO = notifyConfigVO;
    }

    public void setNotifyMQ() {
        NotifyConfigVO notifyConfigVO = new NotifyConfigVO();
        notifyConfigVO.setNotifyType("MQ");
        this.notifyConfigVO = notifyConfigVO;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class NotifyConfigVO {
        /** 回调通知类型 */
        private String notifyType;
        /** 回调通知地址 */
        private String notifyUrl;
        /** 回调消息 */
        private String notifyMsg;
    }

}
