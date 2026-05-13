package top.daoha.trigger.listener;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import top.daoha.api.dto.NotifyRequestDTO;
import top.daoha.api.dto.TeamRefundSuccessRequestDTO;
import top.daoha.domain.order.service.IOrderService;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 结算完成消息监听
 * @create 2025-03-08 13:49
 */
@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private IOrderService orderService;

    /**
     * 指定消费队列
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.consumer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.consumer.topic_team_refund.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.consumer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message) {
        try {
            log.info("rabbit监听任务：：退款回调，发起退款{}", message);
            TeamRefundSuccessRequestDTO teamRefundSuccessRequestDTO = JSON.parseObject(message, TeamRefundSuccessRequestDTO.class);
            String type = teamRefundSuccessRequestDTO.getType();
            if("paid_unformed".equals(type)||"paid_formed".equals(type)){
               orderService.refundOrder(teamRefundSuccessRequestDTO.getUserId(), teamRefundSuccessRequestDTO.getOutTradeNo());
            }
        } catch (Exception e) {
            log.error("拼团回调，退款完成，处理消息失败{}", message, e);
        }
    }

}
