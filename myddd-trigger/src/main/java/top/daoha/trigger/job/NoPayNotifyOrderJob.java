package top.daoha.trigger.job;


import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.daoha.domain.order.service.IOrderService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName : NoPayNotifyOrderJob
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/10  10:02
 */
@Slf4j
@Component
public class NoPayNotifyOrderJob {
    @Resource
    private IOrderService orderService;
    @Resource
    private AlipayClient alipayClient;

    @Scheduled(cron = "0/3 * * * * ?")//每三秒都检查未正确处理的订单是否成功
    public void exec(){
        try {
            log.info("任务：检测未接受到或未正确处理支付回调通知");
            List<String> orderIds=orderService.queryNoPayNotifyOrder();
            if(null == orderIds||orderIds.isEmpty())return;

            for (String id :orderIds){
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(id);
                request.setBizModel(bizModel);

                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();
                // 判断状态码
                if ("10000".equals(code)) {
                    orderService.changeOrderPaySuccess(id);
                }
            }
        }catch (Exception e){
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }
}
