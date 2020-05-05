package com.github.chipolaris.bootforum;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/channel");
    config.setApplicationDestinationPrefixes("/BootForum");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/chat-connect").withSockJS();
  }
  
  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
      registration
          .setMessageSizeLimit(5 * 1024 * 1024) // 5Mb
          .setSendBufferSizeLimit(10 * 1024 * 1024) // 10Mb
          .setSendTimeLimit(2 * 60 * 1000);
  }
}