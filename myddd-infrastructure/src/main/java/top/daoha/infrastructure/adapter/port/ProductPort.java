package top.daoha.infrastructure.adapter.port;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.model.entity.MarketPayDiscountEntity;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.infrastructure.gateway.IGroupBuyMarketService;
import top.daoha.infrastructure.gateway.ProductRPC;
import top.daoha.infrastructure.gateway.dto.LockMarketPayOrderRequestDTO;
import top.daoha.infrastructure.gateway.dto.LockMarketPayOrderResponseDTO;
import top.daoha.infrastructure.gateway.dto.ProductDTO;
import top.daoha.infrastructure.gateway.response.Response;
import top.daoha.types.exception.AppException;

import java.io.IOException;

/**
 * @ClassName : ProductPort
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  23:19
 */
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
                .notifyUrl(notifyUrl)
                .build();

        try {
            Call<Response<LockMarketPayOrderResponseDTO>> responseCall = groupBuyMarketService.lockMarketPayOrderResponse(req);
            Response<LockMarketPayOrderResponseDTO> response = responseCall.execute().body();
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
}
