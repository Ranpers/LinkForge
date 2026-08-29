package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import java.util.UUID;

/** t_user_domain_grant_state 查询行,仅映射用。 */
@SuppressWarnings("unused")
public class GrantStateRow {

    private UUID userId;
    private UUID domainId;
    private boolean granted;
    private long revision;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getDomainId() {
        return domainId;
    }

    public void setDomainId(UUID domainId) {
        this.domainId = domainId;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }
}
