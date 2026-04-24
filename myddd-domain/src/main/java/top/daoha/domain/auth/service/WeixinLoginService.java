package top.daoha.domain.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.google.common.cache.Cache;
import top.daoha.domain.auth.adapter.port.ILoginPort;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private ILoginPort loginPort;

    @Resource
    private Cache<String,String> openidToken;

    @Override
    public String createQrCodeTicket() throws Exception {
        return loginPort.createQrCodeTicket();
    }

    @Override
    public String createQrCodeTicket(String sceneStr) throws Exception {
        String ticket = loginPort.createQrCodeTicket(sceneStr);
        openidToken.put(sceneStr,ticket);
        return ticket;
    }

    @Override
    public String checkLogin(String ticket) {
        return openidToken.getIfPresent(ticket);
    }

    @Override
    public String checkLogin(String ticket, String sceneStr) {
        String cacheTicket = openidToken.getIfPresent(sceneStr);
        if (StringUtils.isBlank(cacheTicket) || !cacheTicket.equals(ticket)) return null;
        return checkLogin(ticket);
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        //保存登录信息
        openidToken.put(ticket, openid);
        //发送模板消息
        loginPort.sendLoginTemplate(openid);
    }
}
