package top.daoha.domain.order.service;

import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.adapter.repository.IOrderRepository;
import top.daoha.domain.order.model.aggregate.CreateOrderAggregate;
import top.daoha.domain.order.model.entity.*;
import top.daoha.domain.order.model.valobj.MarketTypeVO;
import top.daoha.domain.order.model.valobj.OrderStatusVO;
import top.daoha.types.common.Constants;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName : AbstractOrderService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  14:51
 */
@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository iOrderRepository;

    protected final IProductPort iProductPort;

    public AbstractOrderService(IOrderRepository iOrderRepository, IProductPort iProductPort) {
        this.iOrderRepository = iOrderRepository;
        this.iProductPort = iProductPort;
    }

    @Override
    public PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception {
        //1查询当前用户是否存在掉单和未支付订单
        OrderEntity unpaidOrder = iOrderRepository.queryUnPayOrder(shopCartEntity);

        if (null != unpaidOrder && OrderStatusVO.PAY_WAIT.equals(unpaidOrder.getOrderStatusVO())) {
            log.info("创建订单-存在，已存在未支付订单。userId:{} productId:{} orderId:{}",
                    shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrder.getOrderId());
            return PayOrderEntity.builder()
                    .orderId(unpaidOrder.getOrderId())
                    .payUrl(unpaidOrder.getPayUrl())
                    .build();

        } else if (null != unpaidOrder && OrderStatusVO.CREATE.equals(unpaidOrder.getOrderStatusVO())) {
            //缺少支付的url 可能是由于网络原因 超时失败了等情况。。留着需要调用支付宝
            log.info("创建订单-存在，存在未创建支付订单，创建支付单开始。userId:{} productId:{} orderId:{}",
                    shopCartEntity.getUserId(), shopCartEntity.getProductId(), unpaidOrder.getOrderId());

            Integer marketType = unpaidOrder.getMarketType();
            BigDecimal marketDeductionAmount = unpaidOrder.getMarketDeductionAmount();
            //营销锁单
            PayOrderEntity payOrderEntity = null;
            //有参与影响活动但是营销降价为空，为空就说明还有真正的锁单，需要再锁一次
            if (MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(marketType) && null == marketDeductionAmount) {
                MarketPayDiscountEntity marketPayDiscountEntity = lockMarketPayOrder(
                        shopCartEntity.getUserId(),
                        shopCartEntity.getTeamId(),
                        shopCartEntity.getActivityId(),
                        shopCartEntity.getProductId(),
                        unpaidOrder.getOrderId()
                );

                //生成支付订单
                payOrderEntity = doPrepayOrder(shopCartEntity.getProductId(), unpaidOrder.getProductName(),
                        unpaidOrder.getOrderId(), unpaidOrder.getTotalAmount(),marketPayDiscountEntity);

            } else if (MarketTypeVO.GROUP_BUY_MARKET.getCode().equals(marketType)) {

                //生成支付订单
                payOrderEntity = doPrepayOrder(shopCartEntity.getProductId(), unpaidOrder.getProductName(),
                        unpaidOrder.getOrderId(), unpaidOrder.getPayAmount());

            }else {
                //如果当前订单没有参加这个活动，直接使用原始的商品金额进行支付
                payOrderEntity = doPrepayOrder(shopCartEntity.getProductId(), unpaidOrder.getProductName(),
                        unpaidOrder.getOrderId(), unpaidOrder.getTotalAmount());
            }

            return PayOrderEntity.builder()
                    .orderId(payOrderEntity.getOrderId())
                    .payUrl(payOrderEntity.getPayUrl())
                    .build();
        }

        //2 查询商品，创建订单
        ProductEntity productEntity = iProductPort.queryProductByProductId(shopCartEntity.getProductId());

        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(productEntity.getProductId(), productEntity.getProductName(), shopCartEntity.getMarketTypeVO().getCode());



        CreateOrderAggregate build = CreateOrderAggregate.builder()
                .userId(shopCartEntity.getUserId())
                .productEntity(productEntity)
                .orderEntity(orderEntity)
                .build();
        this.doSaveOrder(build);

        //营销锁单
        MarketPayDiscountEntity marketPayDiscountEntity = null;
        if (MarketTypeVO.GROUP_BUY_MARKET.equals(shopCartEntity.getMarketTypeVO())) {
            marketPayDiscountEntity = this.lockMarketPayOrder(
                    shopCartEntity.getUserId(),
                    shopCartEntity.getTeamId(),
                    shopCartEntity.getActivityId(),
                    shopCartEntity.getProductId(),
                    orderEntity.getOrderId()
            );
        }

        PayOrderEntity payOrder1 = this.doPrepayOrder(
                productEntity.getProductId(),
                productEntity.getProductName(),
                orderEntity.getOrderId(),
                productEntity.getPrice(),
                marketPayDiscountEntity
        );
        log.info("创建订单-完成，生成支付单。userid:{} orderid:{} payurl:{}", shopCartEntity.getUserId(), orderEntity.getOrderId(), payOrder1.getPayUrl());
        return PayOrderEntity.builder()
                .orderId(orderEntity.getOrderId())
                .payUrl(payOrder1.getPayUrl())
                .build();
    }

    protected abstract PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal price, MarketPayDiscountEntity marketPayDiscountEntity) throws AlipayApiException;

    protected abstract MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activityId, String productId, String orderId);

    protected abstract PayOrderEntity doPrepayOrder(String productId, String productName, String orderId, BigDecimal totalAmount) throws AlipayApiException;

    protected abstract void doSaveOrder(CreateOrderAggregate build);
}
