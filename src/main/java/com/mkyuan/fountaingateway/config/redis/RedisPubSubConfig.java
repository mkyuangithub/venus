package com.mkyuan.fountaingateway.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.mkyuan.fountaingateway.common.RedisKeyConstants;
@Configuration
public class RedisPubSubConfig {
    protected Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private RouteChangeMessageListener routeChangeMessageListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(routeChangeMessageListener,
                new ChannelTopic(RedisKeyConstants.GATEWAY_ROUTE_CHANGE_TOPIC));
        return container;
    }
}
