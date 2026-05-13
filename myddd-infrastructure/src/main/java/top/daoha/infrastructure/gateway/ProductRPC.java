package top.daoha.infrastructure.gateway;


import org.springframework.stereotype.Service;
import top.daoha.infrastructure.gateway.dto.ProductDTO;

import java.math.BigDecimal;

@Service
public class ProductRPC {

    public ProductDTO queryProductByProductId(String productId){
        ProductDTO productVO = new ProductDTO();
        productVO.setProductId(productId);
        productVO.setProductName("天上的星，不是地上的🦅");
        productVO.setProductDesc("属于你的一刻星星");
        productVO.setPrice(new BigDecimal("100.00"));
        return productVO;
    }
}
