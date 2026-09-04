package io.github.ranpers.linkforge.link.creation.adapter.out.iam;

import io.github.ranpers.linkforge.link.creation.application.IamAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.creation.application.LinkCreationAuthorization;
import io.github.ranpers.linkforge.link.creation.application.port.out.IamAuthorizationGateway;
import io.github.ranpers.linkforge.link.infrastructure.iam.IamClientException;
import io.github.ranpers.linkforge.link.infrastructure.iam.IamServiceClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HttpIamAuthorizationGateway implements IamAuthorizationGateway {

    private final IamServiceClient iamClient;

    public HttpIamAuthorizationGateway(IamServiceClient iamClient) {
        this.iamClient = iamClient;
    }

    @Override
    public LinkCreationAuthorization validate(UUID userId, UUID domainId) {
        try {
            IamLinkCreationAuthorizationResponse response = iamClient.post(
                    "/internal/v1/authorizations/link-creation",
                    new IamLinkCreationAuthorizationRequest(userId, domainId),
                    IamLinkCreationAuthorizationResponse.class
            );
            return new LinkCreationAuthorization(
                    response.allowed(),
                    response.reasonCode(),
                    response.decisionId(),
                    response.evaluatedAt()
            );
        } catch (IamClientException exception) {
            throw new IamAuthorizationUnavailableException("IAM 授权调用失败", exception);
        }
    }
}
