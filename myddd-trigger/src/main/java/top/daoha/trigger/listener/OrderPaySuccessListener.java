package top.daoha.trigger.listener;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName : OrderPaySuccessListener
 * @Description :支付成功回调消息
 * @github:
 * @Author : 24209
 * @Date: 2026/3/10  9:46
 */
@Slf4j
@Component
public class OrderPaySuccessListener {
    @Subscribe
    public void handleEvent(String paySuccessMessage){
        log.info("收到支付成功消息，接下来可做其他事情 {}",paySuccessMessage);
    }
}
