package io.github.ranpers.linkforge.iam;

import io.github.ranpers.linkforge.webmvc.config.LinkForgeWebMvcSupportConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(LinkForgeWebMvcSupportConfiguration.class)
public class LinkForgeIamApplication {
    static void main(String[] args) {
        SpringApplication.run(LinkForgeIamApplication.class, args);
    }
}
