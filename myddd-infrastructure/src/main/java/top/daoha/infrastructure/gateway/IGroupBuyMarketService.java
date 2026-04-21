package top.daoha.infrastructure.gateway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import top.daoha.infrastructure.gateway.dto.LockMarketPayOrderRequestDTO;
import top.daoha.infrastructure.gateway.dto.LockMarketPayOrderResponseDTO;
import top.daoha.infrastructure.gateway.response.Response;

public interface IGroupBuyMarketService {

    @POST("api/v1/gbm/trade/lock_market_pay_order")
    Call<Response<LockMarketPayOrderResponseDTO>> lockMarketPayOrderResponse(@Body LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

}
