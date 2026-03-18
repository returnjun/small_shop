package top.daoha.api;

import top.daoha.api.dto.CreatePayRequestDTO;
import top.daoha.api.response.Response;

/**
 * @ClassName : IPayService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/18  13:32
 */

public interface IPayService {

    public Response<String> createPayOrder(CreatePayRequestDTO createPayRequestDTO);

}
