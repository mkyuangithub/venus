package com.mkyuan.fountaingateway.admin.service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class LoginServiceFactory {
    private final Map<Integer,ILoginService> loginServiceMap = new ConcurrentHashMap<>();


    @Autowired
    public LoginServiceFactory(LoginUsernameAndPassword usernameAndPasswordService) {
        loginServiceMap.put(1, usernameAndPasswordService);
    }

    public ILoginService getLoginService(int loginType) {
        ILoginService loginService = loginServiceMap.get(loginType);
        if (loginService == null) {
            throw new IllegalArgumentException("Unsupported login type: " + loginType);
        }
        return loginService;
    }
}
