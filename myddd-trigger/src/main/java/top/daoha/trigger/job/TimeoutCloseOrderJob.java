package top.daoha.trigger.job;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.daoha.domain.order.service.IOrderService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : TimeoutCloseOrderJob
 * @Description :超时关单
 * @github:
 * @Author : 24209
 * @Date: 2026/3/10  10:05
 */
@Slf4j
@Component
public class TimeoutCloseOrderJob {
    @Resource
    private IOrderService orderService;
    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec(){
        try {
            log.info("任务超时三十分钟直接关闭");
            List<String> orderIds = orderService.queryTimeOutCloseOrderList();
            if(null == orderIds||orderIds.isEmpty()){
                log.info("暂时没有任务超过三十分钟还未支付");
                return;
            }
            for (String id:orderIds){
                boolean status = orderService.changeOrderClose(id);
                log.info("定时任务，超过三十分钟订单关闭 orderId:{} status: {}",orderIds,status);
            }
        }catch (Exception e){
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }
}
