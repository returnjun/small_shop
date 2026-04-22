package top.daoha.test.domain;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import top.daoha.domain.order.model.entity.PayOrderEntity;
import top.daoha.domain.order.model.entity.ShopCartEntity;
import top.daoha.domain.order.model.valobj.MarketTypeVO;
import top.daoha.domain.order.service.IOrderService;

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
public class OrderServiceTest {

    @Resource
    private IOrderService orderService;

    @Test
    public void test_createOrder() throws Exception {
        ShopCartEntity shopCartEntity = new ShopCartEntity();
        shopCartEntity.setUserId("gdk03");
        shopCartEntity.setProductId("9890001");
        shopCartEntity.setTeamId(null);
        shopCartEntity.setActivityId(100123L);
        shopCartEntity.setMarketTypeVO(MarketTypeVO.GROUP_BUY_MARKET);

        PayOrderEntity payOrderEntity = orderService.createOrder(shopCartEntity);
        log.info("请求参数:{}", JSON.toJSONString(shopCartEntity));
        log.info("测试结果:{}", JSON.toJSONString(payOrderEntity));
    }
}
