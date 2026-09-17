package io.github.ranpers.linkforge.link;

import io.github.ranpers.linkforge.webmvc.config.LinkForgeWebMvcSupportConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(LinkForgeWebMvcSupportConfiguration.class)
public class LinkForgeLinkApplication {
    static void main(String[] args) {
        SpringApplication.run(LinkForgeLinkApplication.class, args);
    }
}
