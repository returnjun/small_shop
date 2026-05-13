package top.daoha.api;

import top.daoha.api.dto.*;
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

    String groupBuyNotify(NotifyRequestDTO notifyRequestDTO);

    /**
     * 查询用户订单列表
     *
     * @param requestDTO 请求对象
     * @return 订单列表
     */
    Response<QueryOrderListResponseDTO> queryUserOrderList(QueryOrderListRequestDTO requestDTO);

    /**
     * 用户退单
     *
     * @param requestDTO 请求对象
     * @return 退单结果
     */
    Response<RefundOrderResponseDTO> refundOrder(RefundOrderRequestDTO requestDTO);

}
