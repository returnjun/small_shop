package top.daoha.domain.auth.adapter.port;

import java.io.IOException;

/**
 * @ClassName : ILoginPort
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/14  9:54
 */

public interface ILoginPort {

    String createQrCodeTicket() throws Exception;

    void sendLoginTemplate(String openid) throws IOException;

    String createQrCodeTicket(String sceneStr) throws IOException;
}
