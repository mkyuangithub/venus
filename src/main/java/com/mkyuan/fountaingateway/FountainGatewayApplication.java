package com.mkyuan.fountaingateway;


import com.mkyuan.fountaingateway.admin.bean.UserInfo;
import com.mkyuan.fountaingateway.common.util.EncryptUtil;
import com.mkyuan.fountaingateway.common.util.MD5Util;
import com.mkyuan.fountaingateway.gateway.service.RouteService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.PostConstruct;


@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RedissonAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class
})
@EnableDiscoveryClient
public class FountainGatewayApplication {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private MongoTemplate mongoTemplate;

    //@Value("${security.key}")
    //private String securityKey = "";

    //@Value("${token.signature}")
    //private String tokenSignature = "";

    @Value("${venus.admin_default_password}")
    private String venusAdminDefaultPassword;

    @Autowired
    private RouteService routeService;

    public static void main(String[] args) {
        SpringApplication.run(FountainGatewayApplication.class, args);
    }
    @PostConstruct
    public void init(){
        initAdminPwd();
        this.routeService.refreshAllDataToRedis();
    }
    private void initAdminPwd() {
        try{
            // 检查管理员用户是否存在
            if (!checkAdminUserInMongo()) {
                // 创建管理员用户
                String adminLoginPwd= MD5Util.getMD5(venusAdminDefaultPassword);
                UserInfo adminUser = new UserInfo();
                adminUser.setLoginId("admin");
                adminUser.setLoginPwd(adminLoginPwd);
                // 设置其他必要的字段
                mongoTemplate.save(adminUser, "UserInfo");
                logger.info(">>>>>>init admin user  successfully");
            }else{
                logger.info(">>>>>>there is a admin user, no need init");
            }

        }catch(Exception e){
            logger.error(">>>>>>init admin user error->{}",e.getMessage(),e);
        }
    }

    private boolean checkAdminUserInMongo(){
        boolean isExisted = false;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("loginId").is("admin"));

            // 检查是否存在管理员用户
            isExisted = mongoTemplate.exists(query, UserInfo.class, "UserInfo");

            logger.info(">>>>>>Admin user exists: {}", isExisted);
        } catch (Exception e) {
            logger.error(">>>>>>Check admin user error: {}", e.getMessage(), e);
        }
        return isExisted;
    }
}
