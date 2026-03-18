package top.daoha.api.dto;

import lombok.Data;

/**
 * @ClassName : CreatePayRequestDTO
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  13:33
 */
@Data
public class CreatePayRequestDTO {
    //用户id
    private String userId;
    //产品id
    private String productId;
}
