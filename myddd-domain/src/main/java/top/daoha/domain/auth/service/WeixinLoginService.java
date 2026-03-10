package top.daoha.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.google.common.cache.Cache;
import javax.annotation.Resource;
import java.io.IOException;

/**
 * @ClassName : WeixinLoginService
 * @Description :
 * @github:
 * @Author : 24209
 * @Date: 2026/3/10  21:22
 */
@Slf4j
@Service
public class WeixinLoginService implements ILoginService{

    @Resource
    private Cache<String,String> openidToken;
    @Override
    public String createQrCodeTicket() throws Exception {
        return null;
    }

    @Override
    public String checkLogin(String ticket) {
        return null;
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {

    }
}
