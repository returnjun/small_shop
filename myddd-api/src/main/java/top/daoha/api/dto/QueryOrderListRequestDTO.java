package top.daoha.api.dto;

import lombok.Data;

@Data
public class QueryOrderListRequestDTO {

    /** 用户ID */
    private String userId;
    /** 分页参数：大于此ID的记录 */
    private Long lastId;
    /** 每页数量 */
    private Integer pageSize = 10;
    /** 订单状态 0:全部 1:待支付 2：代发货 3：已发货 */
    private Integer orderStatus = 0;

}