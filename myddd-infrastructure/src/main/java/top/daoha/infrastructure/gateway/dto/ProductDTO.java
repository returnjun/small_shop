package top.daoha.infrastructure.gateway.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName : ProductEntity
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/17  21:45
 */

@Data
public class ProductDTO {
    /** 商品ID */
    private String productId;
    /** 商品名称 */
    private String productName;
    /** 商品描述 */
    private String productDesc;
    /** 商品价格 */
    private BigDecimal price;
}
