package com.mkyuan.fountaingateway.admin.controller;

import com.mkyuan.fountaingateway.admin.bean.LoginBean;
import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.admin.service.ILoginService;
import com.mkyuan.fountaingateway.admin.service.LoginServiceFactory;
import com.mkyuan.fountaingateway.common.util.EncryptUtil;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
@RestController
@RequestMapping("/gateway/admin")
public class GatewayUserLoginController {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private LoginServiceFactory loginServiceFactory;

    @Value("${security.key}")
    private String securityKey = "";

    @PostMapping("/login")
    public ResponseEntity<UserInfo> userLogin(@RequestBody  JSONObject params) {
        try {
            LoginBean loginBean=new LoginBean();
            String encryptedLoginPwd=params.getString("loginPwd");
            String decryptLoginPwd="";
            String loginId=params.getString("loginId");
            try{
                decryptLoginPwd= EncryptUtil.decrypt_safeencode(encryptedLoginPwd,securityKey);
            }catch(Exception e){
                logger.error(">>>>>> encrypt error->{}",e.getMessage(),e);
                decryptLoginPwd="";
            }
            loginBean=new LoginBean();
            loginBean.setLoginId(loginId);
            loginBean.setLoginPwd(decryptLoginPwd);
            ILoginService loginService= loginServiceFactory.getLoginService(1);
            UserInfo userInfo=loginService.doLogin(loginBean);
            logger.info(">>>>>>process user login finished");
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            logger.error(">>>>>>userLogin API error->{}",e.getMessage(),e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
