package top.daoha.infrastructure.adapter.port;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import top.daoha.domain.order.adapter.port.IProductPort;
import top.daoha.domain.order.model.entity.ProductEntity;
import top.daoha.infrastructure.gateway.ProductRPC;
import top.daoha.infrastructure.gateway.dto.ProductDTO;

/**
 * @ClassName : ProductPort
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  23:19
 */
@Component
public class ProductPort implements IProductPort {

    private final ProductRPC productRPC;

    public ProductPort(ProductRPC productRPC) {
        this.productRPC=productRPC;
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
}
