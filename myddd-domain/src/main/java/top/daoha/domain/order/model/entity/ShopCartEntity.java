package top.daoha.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.daoha.domain.order.model.valobj.MarketTypeVO;

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
    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 营销类型，无营销、拼团营销 */
    private MarketTypeVO marketTypeVO;


}
