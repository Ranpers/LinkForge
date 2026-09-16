package io.github.ranpers.linkforge.link.infrastructure.iam;

import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class IamServiceClient {

    private static final String SERVICE_PRINCIPAL = "linkforge-link-service";

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String registrationId;

    public IamServiceClient(
            RestClient iamRestClient,
            OAuth2AuthorizedClientManager iamAuthorizedClientManager,
            IamClientProperties properties
    ) {
        this.restClient = iamRestClient;
        this.authorizedClientManager = iamAuthorizedClientManager;
        this.registrationId = properties.getRegistrationId();
    }

    public <T> T post(String uri, Object requestBody, Class<T> responseType) {
        try {
            var authorizeRequest = OAuth2AuthorizeRequest
                    .withClientRegistrationId(registrationId)
                    .principal(SERVICE_PRINCIPAL)
                    .build();
            var client = authorizedClientManager.authorize(authorizeRequest);
            if (client == null || client.getAccessToken() == null) {
                throw new IamClientException("无法取得 IAM 服务令牌");
            }
            T response = restClient.post()
                    .uri(uri)
                    .headers(headers -> headers.setBearerAuth(
                            client.getAccessToken().getTokenValue()
                    ))
                    .body(requestBody)
                    .retrieve()
                    .body(responseType);
            if (response == null) {
                throw new IamClientException("IAM 响应为空");
            }
            return response;
        } catch (RestClientException exception) {
            throw new IamClientException("IAM 调用失败", exception);
        }
    }
}
