package io.github.ranpers.linkforge.link.management.adapter.out.iam;

import io.github.ranpers.linkforge.link.infrastructure.iam.IamClientException;
import io.github.ranpers.linkforge.link.infrastructure.iam.IamServiceClient;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAction;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorization;
import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.management.application.port.out.LinkManagementAuthorizationGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HttpIamLinkManagementAuthorizationGateway
        implements LinkManagementAuthorizationGateway {

    private final IamServiceClient iamClient;

    public HttpIamLinkManagementAuthorizationGateway(IamServiceClient iamClient) {
        this.iamClient = iamClient;
    }

    @Override
    public LinkManagementAuthorization validate(
            UUID actorUserId,
            UUID domainId,
            UUID createdByUserId,
            LinkManagementAction action
    ) {
        try {
            IamLinkManagementAuthorizationResponse response = iamClient.post(
                    "/internal/v1/authorizations/link-management",
                    new IamLinkManagementAuthorizationRequest(
                            actorUserId,
                            domainId,
                            createdByUserId,
                            action
                    ),
                    IamLinkManagementAuthorizationResponse.class
            );
            return new LinkManagementAuthorization(
                    response.allowed(),
                    response.reasonCode(),
                    response.decisionId(),
                    response.evaluatedAt()
            );
        } catch (IamClientException exception) {
            throw new LinkManagementAuthorizationUnavailableException(
                    "IAM 管理授权调用失败",
                    exception
            );
        }
    }
}
