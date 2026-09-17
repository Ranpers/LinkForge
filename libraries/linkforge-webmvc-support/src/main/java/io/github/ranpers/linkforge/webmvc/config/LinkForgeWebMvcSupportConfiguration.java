package io.github.ranpers.linkforge.webmvc.config;

import io.github.ranpers.linkforge.webmvc.problem.ApiSecurityProblemHandler;
import io.github.ranpers.linkforge.webmvc.request.RequestTraceFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * 显式注册 LinkForge WebMVC 公共组件。
 *
 * <p>本模块不在服务启动类的组件扫描范围内，需要使用的服务必须在启动类上
 * {@code @Import} 本配置，才能获得请求关联过滤器与安全异常出口。</p>
 *
 * @apiNote 仅有状态、需要由容器管理生命周期的组件在此注册；{@code ApiProblems}、
 * {@code CursorCodec} 等无状态工具类保持为普通静态工具，不作为 Bean。
 */
@Configuration(proxyBeanMethods = false)
public class LinkForgeWebMvcSupportConfiguration {

    /**
     * 注册请求关联过滤器。
     *
     * @return 优先级为最高、在安全过滤器链之前执行的过滤器
     */
    @Bean
    RequestTraceFilter requestTraceFilter() {
        return new RequestTraceFilter();
    }

    /**
     * 注册统一的 401 与 403 问题响应出口。
     *
     * @param objectMapper 序列化问题响应的 Jackson 映射器
     * @return 同时实现认证入口点与拒绝处理器的出口
     */
    @Bean
    ApiSecurityProblemHandler apiSecurityProblemHandler(ObjectMapper objectMapper) {
        return new ApiSecurityProblemHandler(objectMapper);
    }
}
