package top.daoha.domain.auth.service;

import java.io.IOException;

/**
 * @ClassName : ILoginService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/2/24  14:51
 */

public interface ILoginService {

    String createQrCodeTicket()throws Exception;

    String createQrCodeTicket(String sceneStr)throws Exception;

    String checkLogin(String ticket);

    String checkLogin(String ticket,String sceneStr);

    void saveLoginState(String ticket,String openid) throws IOException;

}
