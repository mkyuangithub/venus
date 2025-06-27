package com.mkyuan.fountaingateway.config.redis;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher {

    @Autowired
    private  RedisTemplate redisTemplate;

    @Value("${spring.application.instance-id:${random.uuid}}")
    private String instanceId;



    public void publishRouteChangeMessage(String action, String routeId) {
        // 格式: action|routeId|instanceId
        String message = action + "|" + routeId + "|" + instanceId;
        redisTemplate.convertAndSend(RedisKeyConstants.GATEWAY_ROUTE_CHANGE_TOPIC, message);
    }
}
