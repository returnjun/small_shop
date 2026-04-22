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
import java.util.Date;
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

    @Scheduled(cron = "0/30 * * * * ?")//每三秒都检查未正确处理的订单是否成功
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
                    // 必须追加这一步：获取真实的交易状态
                    String tradeStatus = alipayTradeQueryResponse.getTradeStatus();

                    // 只有当支付宝明确说“交易成功”或“交易结束”时，才算真正付了钱
                    if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                        log.info("订单 {} 确认支付成功，准备修改本地状态", id);
                        orderService.changeOrderPaySuccess(id, alipayTradeQueryResponse.getSendPayDate());
                    } else {
                        // 用户可能还在犹豫，或者交易已经关闭，这里不用处理，只打个日志即可
                        log.info("订单 {} 查询成功，但尚未支付，当前状态为: {}", id, tradeStatus);
                    }
                } else {
                    log.error("订单 {} 调用支付宝查询接口失败，原因: {}", id, alipayTradeQueryResponse.getSubMsg());
                }
            }
        }catch (Exception e){
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }
}
