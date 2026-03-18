package top.daoha.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : ShopCartEntity
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/16  11:57
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopCartEntity {

    private String userId;

    private String productId;
}
