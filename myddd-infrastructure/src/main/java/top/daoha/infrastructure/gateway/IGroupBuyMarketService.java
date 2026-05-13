package top.daoha.infrastructure.gateway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import top.daoha.infrastructure.gateway.dto.*;
import top.daoha.infrastructure.gateway.response.Response;

public interface IGroupBuyMarketService {

    //这个就是用来调用拼团的锁单接口，可以返回锁单后的商品支付信息用于以后的支付调用
    @POST("api/v1/gbm/trade/lock_market_pay_order")
    Call<Response<LockMarketPayOrderResponseDTO>> lockMarketPayOrder(@Body LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

    //这个支付完成以后调用这个接口
    @POST("api/v1/gbm/trade/settlement_market_pay_order")
    Call<Response<SettlementMarketPayOrderResponseDTO>> settlementMarketPayOrder(@Body SettlementMarketPayOrderRequestDTO settlementMarketPayOrderRequestDTO);

    //这个退单完成以后调用这个接口告诉拼团系统退单
    @POST("api/v1/gbm/trade/refund_market_pay_order")
    Call<Response<RefundMarketPayOrderResponseDTO>> refundMarketPayOrder(@Body RefundMarketPayOrderRequestDTO refundMarketPayOrderRequestDTO);

}
