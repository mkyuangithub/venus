package com.mkyuan.fountaingateway.admin.service;

import com.mkyuan.fountaingateway.admin.bean.LoginBean;
import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import com.mkyuan.fountaingateway.common.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LoginUsernameAndPassword implements ILoginService{

    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${token.signature}")
    private String tokenSignature = "";

    @Autowired
    private RedisTemplate redisTemplate;

    public boolean verifiedLogin(LoginBean loginBean)throws Exception{
       boolean result=false;
       String mongoCollection="UserInfo";
       if(loginBean==null){
           return false;
       }
       if(StringUtils.isBlank(loginBean.getLoginId())||StringUtils.isBlank(loginBean.getLoginPwd())){
           return false;
       }
       try{
            String md5Pwd=MD5Util.getMD5(loginBean.getLoginPwd());//解密后的密码的md5去UserInfo比较
            Query query=new Query();
            query.addCriteria(Criteria.where("loginId").is(loginBean.getLoginId()));
            query.addCriteria(Criteria.where("loginPwd").is(md5Pwd));
            UserInfo userInfo=mongoTemplate.findOne(query,UserInfo.class,mongoCollection);
            if(userInfo!=null){
                return true;
            }
       }catch(Exception e){
           throw new Exception(">>>>>>verified user's login action error->+"+e.getMessage(),e);
       }
       return result;
    }

    public UserInfo doLogin(LoginBean loginBean)throws Exception{
        String ut="";
        UserInfo userInfo=new UserInfo();
        try{
            Boolean verifiedResult=this.verifiedLogin(loginBean);
            if(verifiedResult){
                String redisKey= RedisKeyConstants.GATEWAY_USER_LOGIN+loginBean.getLoginId();
                if(redisTemplate.hasKey(redisKey)){ //对于已登录重复登录用户先删除登录信息
                    redisTemplate.delete(redisKey);
                }
                String token= MD5Util.generateToken(loginBean.getLoginId(),tokenSignature); //使用token:signature来进行token的获取
                userInfo=new UserInfo();
                userInfo.setLoginStatus(1);
                userInfo.setBizType(loginBean.getBizType());
                userInfo.setUt(token);
                userInfo.setLastLoginDate(new Date());
                redisTemplate.opsForValue().set(redisKey,userInfo);
                return userInfo;
            }else{
                userInfo=new UserInfo();
                userInfo.setLoginStatus(2);//用户名密码错误
                return userInfo;
            }
        }catch(Exception e){
            throw new Exception("user do login error->"+e.getMessage(),e);
        }
    }
}
