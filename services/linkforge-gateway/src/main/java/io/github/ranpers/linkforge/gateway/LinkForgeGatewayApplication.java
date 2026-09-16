package io.github.ranpers.linkforge.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class LinkForgeGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkForgeGatewayApplication.class, args);
    }
}
