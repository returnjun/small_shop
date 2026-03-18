package top.daoha.domain.order.adapter.port;

import top.daoha.domain.order.model.entity.ProductEntity;

/**
 * @ClassName : IProducrPort
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  14:55
 */

public interface IProductPort {
    ProductEntity queryProductByProductId(String productId);
}
