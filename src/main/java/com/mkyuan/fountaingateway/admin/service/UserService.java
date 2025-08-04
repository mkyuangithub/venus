package com.mkyuan.fountaingateway.admin.service;

import com.mkyuan.fountaingateway.admin.bean.LoginBean;
import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private RedisTemplate redisTemplate;

    public void logout(UserInfo userInfo){
        String redisKey= RedisKeyConstants.GATEWAY_USER_LOGIN+userInfo.getLoginId();
        try{
            boolean userExisted=redisTemplate.hasKey(redisKey);
            if(userExisted){
                logger.info(">>>>>正在操作的用户->{}存在",redisKey);
                UserInfo existedUser=(UserInfo) redisTemplate.opsForValue().get(redisKey);
                logger.info(">>>>>正在操作的用户传入的token->{}",userInfo.getUt());
                if(userInfo.getUt().equals(existedUser.getUt())){
                    logger.info(">>>>>正在操作的用户实际在redis内的token->{}，两者匹配",existedUser.getUt());
                    redisTemplate.delete(redisKey);
                }else{
                    logger.info(">>>>>正在操作的用户实际在redis内的token->{}，两者不匹配",existedUser.getUt());
                }
            }
        }catch(Exception e){
            logger.error(">>>>>>User do logout error->{}",e.getMessage(),e);
        }
    }
}
