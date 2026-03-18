package top.daoha.domain.order.adapter.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import top.daoha.types.event.BaseEvent;

import java.util.Date;

/**
 * @ClassName : PaySuccessMessageEvent
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  16:32
 */
@Component
public class PaySuccessMessageEvent extends BaseEvent<PaySuccessMessageEvent.PaySuccessMessage> {


    @Override
    public EventMessage<PaySuccessMessageEvent.PaySuccessMessage> buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage data) {
        return EventMessage.<PaySuccessMessage>builder()
                .id(RandomStringUtils.randomNumeric(11))
                .timestamp(new Date())
                .data(data)
                .build();
    }

    @Override
    public String topic() {
        return "pay_success";
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaySuccessMessage{
        private String userId;
        private String tradeNo;
    }
}
