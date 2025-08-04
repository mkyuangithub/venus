package com.mkyuan.fountaingateway.admin.controller;

import com.mkyuan.fountaingateway.admin.bean.LoginBean;
import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.admin.service.ILoginService;
import com.mkyuan.fountaingateway.admin.service.LoginServiceFactory;
import com.mkyuan.fountaingateway.admin.service.UserService;
import com.mkyuan.fountaingateway.common.controller.response.ResponseBean;
import com.mkyuan.fountaingateway.common.controller.response.ResponseCodeEnum;
import com.mkyuan.fountaingateway.common.util.EncryptUtil;
import com.mkyuan.fountaingateway.gateway.model.GatewayRouteDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;
@RestController
@RequestMapping("/venus") // 添加统一的基础路径

public class GatewayUserLoginController {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private LoginServiceFactory loginServiceFactory;

    @Value("${security.key}")
    private String securityKey = "";

    @Autowired
    private UserService userService;

    @PostMapping("/api/admin/login")
    public ResponseBean userLogin(@RequestBody  JSONObject params) {
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
            return new ResponseBean(ResponseCodeEnum.SUCCESS,userInfo);
        } catch (Exception e) {
            logger.error(">>>>>>userLogin API error->{}",e.getMessage(),e);
            return new ResponseBean(ResponseCodeEnum.FAIL);
        }
    }


    @PostMapping("/api/admin/logout")
    public ResponseBean logout(@RequestHeader("token") String token, @RequestBody  JSONObject params) {
        try {
            String decryptToken="";
            LoginBean loginBean=new LoginBean();
            String loginId=params.getString("loginId");
            try{
                decryptToken=EncryptUtil.decrypt_safeencode(token,securityKey);
            }catch(Exception e){
                decryptToken=token;
            }
            UserInfo userInfo=new UserInfo();
            userInfo.setLoginId(loginId);
            userInfo.setUt(decryptToken);
            userService.logout(userInfo);
            logger.info(">>>>>>user logout successfully");
            return new ResponseBean(ResponseCodeEnum.SUCCESS,userInfo);
        } catch (Exception e) {
            logger.error(">>>>>>user logout API error->{}",e.getMessage(),e);
            return new ResponseBean(ResponseCodeEnum.FAIL);
        }
    }
}
