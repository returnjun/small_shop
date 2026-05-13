package top.daoha.test.trigger;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.daoha.api.IPayService;
import top.daoha.api.dto.QueryOrderListRequestDTO;
import top.daoha.api.dto.QueryOrderListResponseDTO;
import top.daoha.api.response.Response;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.MarketTypeVO;
import top.daoha.domain.order.service.IOrderService;
import top.daoha.trigger.http.AliPayController;

import javax.annotation.Resource;

/**
 * @ClassName : OrderServiceTest
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  9:32
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class queryOrderTest {

    @Resource
    private IPayService aliPayController;

    @Test
    public void test_createOrder() throws Exception {
        QueryOrderListRequestDTO requestDTO = new QueryOrderListRequestDTO();
        requestDTO.setUserId("gdk01");
        requestDTO.setOrderStatus(0);


        Response<QueryOrderListResponseDTO> response = aliPayController.queryUserOrderList(requestDTO);
        log.info("请求参数:{}", JSON.toJSONString(requestDTO));
        log.info("测试结果:{}", JSON.toJSONString(response));
    }
}
