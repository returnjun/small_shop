package top.daoha.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import top.daoha.domain.goods.service.IGoodsService;
import top.daoha.domain.order.adapter.event.PaySuccessMessageEvent;

import javax.annotation.Resource;

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

    @Resource
    private IGoodsService iGoodsService;



    @Subscribe
    public void handleEvent(String paySuccessMessageJson){
        log.info("收到支付成功消息: {}",paySuccessMessageJson);

        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = JSON.parseObject(paySuccessMessageJson, PaySuccessMessageEvent.PaySuccessMessage.class);

        log.info("模拟发货，单号:{}",paySuccessMessage.getTradeNo());

        iGoodsService.changeOrderDealDone(paySuccessMessage.getTradeNo());
    }
}
