package top.daoha.infrastructure.adapter.port;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.model.entity.MarketPayDiscountEntity;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.infrastructure.gateway.IGroupBuyMarketService;
import top.daoha.infrastructure.gateway.ProductRPC;
import top.daoha.infrastructure.gateway.dto.*;
import top.daoha.infrastructure.gateway.response.Response;
import top.daoha.types.exception.AppException;

import java.io.IOException;
import java.util.Date;

/**
 * @ClassName : ProductPort
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  23:19
 */
@Slf4j
@Component
public class ProductPort implements IProductPort {
    @Value("${app.config.group-buy-market.source}")
    private String source;
    @Value("${app.config.group-buy-market.channel}")
    private String channel;
    @Value("${app.config.group-buy-market.notify-url}")
    private String notifyUrl;

    private final ProductRPC productRPC;

    private final IGroupBuyMarketService groupBuyMarketService;

    public ProductPort(ProductRPC productRPC, IGroupBuyMarketService groupBuyMarketService) {
        this.productRPC = productRPC;
        this.groupBuyMarketService = groupBuyMarketService;
    }

    @Override
    public ProductEntity queryProductByProductId(String productId) {
        ProductDTO productDTO= productRPC.queryProductByProductId(productId);
        return ProductEntity.builder()
                .productId(productDTO.getProductId())
                .productName(productDTO.getProductName())
                .productDesc(productDTO.getProductDesc())
                .price(productDTO.getPrice())
                .build();
    }

    @Override
    public MarketPayDiscountEntity lockMarketPayOrder(String userId, String teamId, Long activity, String productId, String orderId) {

        LockMarketPayOrderRequestDTO req = LockMarketPayOrderRequestDTO.builder()
                .userId(userId)
                .teamId(teamId)
                .activityId(activity)
                .goodsId(productId)
                .outTradeNo(orderId)
                .source(source)
                .channel(channel)
//                .notifyUrl(notifyUrl)
                .build();
        req.setNotifyMQ();

        try {
            Call<Response<LockMarketPayOrderResponseDTO>> responseCall = groupBuyMarketService.lockMarketPayOrder(req);
            Response<LockMarketPayOrderResponseDTO> response = responseCall.execute().body();
            log.info("营销锁单成功，请检查访问相关接口的结果 response:{}",response);
            if(null==response){
                return null;
            }
            if(!"0000".equals(response.getCode())){
                throw new AppException(response.getCode(),response.getInfo());
            }
            LockMarketPayOrderResponseDTO data = response.getData();
            return MarketPayDiscountEntity.builder()
                    .originalPrice(data.getOriginalPrice())
                    .payPrice(data.getPayPrice())
                    .marketDeductionAmount(data.getDeductionPrice()).build();

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void settlementMarketPayOrder(String userId, String orderId, Date orderTime) {
        SettlementMarketPayOrderRequestDTO requestDTO = SettlementMarketPayOrderRequestDTO.builder()
                .userId(userId)
                .outTradeNo(orderId)
                .outTradeTime(orderTime)
                .source(source)
                .channel(channel)
                .build();

        try {
            Call<Response<SettlementMarketPayOrderResponseDTO>> responseCall = groupBuyMarketService.settlementMarketPayOrder(requestDTO);

            Response<SettlementMarketPayOrderResponseDTO> response = responseCall.execute().body();
            log.info("营销锁单成功，请检查访问相关接口的结果 response:{}",response);
            if(null==response){
                return ;
            }
            if(!"0000".equals(response.getCode())){
                throw new AppException(response.getCode(),response.getInfo());
            }

        }catch (Exception e){
            log.info("营销锁单失败，错误异常信息:{}",e);
        }

    }

    @Override
    public void refundMarketPayOrder(String userId, String orderId) throws IOException {
        RefundMarketPayOrderRequestDTO requestDTO = RefundMarketPayOrderRequestDTO.builder()
                .userId(userId)
                .outTradeNo(orderId)
                .source(source)
                .channel(channel)
                .build();
        try {
            Call<Response<RefundMarketPayOrderResponseDTO>> responseCall = groupBuyMarketService.refundMarketPayOrder(requestDTO);

            Response<RefundMarketPayOrderResponseDTO> response = responseCall.execute().body();
            log.info("营销退单行为成功，请检查访问相关接口的结果 response:{}",response);
            if(null==response){
                return ;
            }
            if(!"0000".equals(response.getCode())){
                throw new AppException(response.getCode(), response.getInfo());
            }
        }catch (Exception e){
            log.error("营销退单行为失败，错误异常信息:{}", JSON.toJSONString(e));
            // 关键：把异常继续往上传递，这样上层逻辑才会中断
            throw e;
        }
    }
}
