package com.mkyuan.fountaingateway.config.redis;
import com.mkyuan.fountaingateway.gateway.MongoRouteDefinitionLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.connection.Message;
@Component
public class RouteChangeMessageListener implements MessageListener {
    protected Logger logger = LogManager.getLogger(this.getClass());
    @Autowired
    private MongoRouteDefinitionLocator routeDefinitionLocator;

    @Value("${spring.application.instance-id:${random.uuid}}")
    private String instanceId;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageStr = new String(message.getBody());
            String[] parts = messageStr.split("\\|");

            if (parts.length < 3) {
                logger.warn("无效的路由变更消息格式: {}", messageStr);
                return;
            }

            String action = parts[0];
            String routeId = parts[1];
            String sourceInstanceId = parts[2];

            // 如果消息不是由当前实例发送的，执行刷新
            if (!instanceId.equals(sourceInstanceId)) {
                logger.info(">>>>>>收到其他实例的路由变更消息: action={}, routeId={}, from={}, 执行刷新",
                        action, routeId, sourceInstanceId);
                routeDefinitionLocator.refresh();
            } else {
                logger.info(">>>>>>收到本实例发送的路由变更消息: action={}, routeId={}, 不执行刷新",
                        action, routeId);
            }
        } catch (Exception e) {
            logger.error("处理路由变更消息失败: {}", e.getMessage(), e);
        }
    }
}
