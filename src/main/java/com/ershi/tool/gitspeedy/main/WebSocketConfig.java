package com.ershi.tool.gitspeedy.main;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * Description: TODO <br/>
 * Copyright: (c) 2023 SunTime Co'Ltd Inc. All rights reserved.<br/>
 *
 * @author 沈科
 * @version 1.0
 * @date 2023/1/12 17:35
 * @since JDK11
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}
