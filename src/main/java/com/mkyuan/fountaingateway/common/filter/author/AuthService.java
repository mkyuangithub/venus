package com.mkyuan.fountaingateway.common.filter.author;

import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import com.mkyuan.fountaingateway.common.controller.response.ResponseBean;
import com.mkyuan.fountaingateway.common.controller.response.ResponseCodeEnum;
import com.mkyuan.fountaingateway.common.util.EncryptUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuthService {
    protected Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${security.key}")
    private String securityKey = "";

    public ResponseBean checkUserAuth(String token, String loginId) {
        logger.info(">>>>>>当前需要认证的loginId->{}, 加密的token->{}",loginId,token);
        if (token == null || loginId == null) {
            return new ResponseBean(ResponseCodeEnum.LOGIN_ERROR, null);
        }

        String decryptToken;
        try {
            decryptToken = EncryptUtil.decrypt_safeencode(token, securityKey);
        } catch(Exception e) {
            decryptToken = token;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setLoginId(loginId);
        userInfo.setUt(decryptToken);

        String redisKey = RedisKeyConstants.GATEWAY_USER_LOGIN + userInfo.getLoginId();
        try {
            boolean userExisted = redisTemplate.hasKey(redisKey);
            if (userExisted) {
                logger.info(">>>>>正在操作的用户->{}存在", redisKey);
                UserInfo existedUser = (UserInfo) redisTemplate.opsForValue().get(redisKey);
                logger.info(">>>>>正在操作的用户传入的token->{}", userInfo.getUt());
                if (userInfo.getUt().equals(existedUser.getUt())) {
                    logger.info(">>>>>正在操作的用户实际在redis内的token->{}，两者匹配", existedUser.getUt());
                    return new ResponseBean(ResponseCodeEnum.SUCCESS, userInfo);
                } else {
                    logger.info(">>>>>正在操作的用户实际在redis内的token->{}，两者不匹配", existedUser.getUt());
                    return new ResponseBean(ResponseCodeEnum.LOGIN_ERROR, userInfo);
                }
            } else {
                return new ResponseBean(ResponseCodeEnum.LOGIN_ERROR, userInfo);
            }
        } catch(Exception e) {
            logger.error(">>>>>>调用时出错->{}", e.getMessage(), e);
            return new ResponseBean(ResponseCodeEnum.FAIL, userInfo);
        }
    }
}
