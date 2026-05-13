package top.daoha.domain.order.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCount {
    /** 对应数据库的 order_status 字段 */
    private String status;

    /** 对应 COUNT(*) 的统计结果 */
    private Integer count;
}