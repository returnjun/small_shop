package top.daoha.api;

import top.daoha.api.response.Response;

/**
 * @ClassName : IAuthService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/15  16:12
 */

public interface IAuthService {

    Response<String> weixinQeCodeTicket();

    Response<String> checkLogin(String ticket);

}
