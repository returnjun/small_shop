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

    //上面两个是生成ticket
    Response<String> weixinQeCodeTicket();

    Response<String> weixinQeCodeTicket(String sceneStr);

    //下面两个是校验ticket
    Response<String> checkLogin(String ticket);

    Response<String> checkLogin(String ticket,String sceneStr);

}
