package io.github.ranpers.linkforge.link.infrastructure.iam;

import io.github.ranpers.linkforge.webmvc.request.RequestIdContext;
import java.io.IOException;
import java.net.http.HttpClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(IamClientProperties.class)
public class IamClientConfiguration {

    @Bean
    OAuth2AuthorizedClientManager iamAuthorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService clients
    ) {
        return new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clients);
    }

    @Bean
    @LoadBalanced
    RestClient.Builder iamLoadBalancedRestClientBuilder(RestClientBuilderConfigurer configurer) {
        return configurer.configure(RestClient.builder());
    }

    @Bean
    RestClient iamRestClient(
            IamClientProperties properties,
            @Qualifier("iamLoadBalancedRestClientBuilder") RestClient.Builder builder
    ) {
        properties.validate();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return builder
                .clone()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(new RequestIdPropagationInterceptor())
                .build();
    }

    /**
     * 把当前请求的关联标识带给 IAM，使跨服务调用在日志中仍可串联。
     *
     * @implNote 只处理 {@link RequestIdContext#HEADER_NAME}，不干预 OpenTelemetry 传播的
     * {@code traceparent}。调用发生在非请求线程（如定时任务）时不写入该头，由接收方自行生成。
     */
    private static final class RequestIdPropagationInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution
        ) throws IOException {
            String requestId = RequestIdContext.currentRequestId();
            if (requestId != null) {
                request.getHeaders().set(RequestIdContext.HEADER_NAME, requestId);
            }
            return execution.execute(request, body);
        }
    }
}
