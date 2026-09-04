package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;
import io.github.ranpers.linkforge.iam.security.application.port.out.LinkSecurityRestrictionStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisLinkSecurityRestrictionStore implements LinkSecurityRestrictionStore {

    private final LinkSecurityRestrictionMapper mapper;

    public MybatisLinkSecurityRestrictionStore(LinkSecurityRestrictionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CreateResult create(CreateLinkSecurityRestrictionCommand command) {
        if (!Boolean.TRUE.equals(mapper.actorAllowed(command.actorUserId()))) {
            return new CreateResult(MutationOutcome.DENIED, null);
        }
        if (mapper.lockTargetUser(command.targetUserId()) == null) {
            return new CreateResult(MutationOutcome.TARGET_NOT_FOUND, null);
        }
        UUID restrictionId = mapper.insertRestriction(
                command.targetUserId(),
                command.restriction().mode().name(),
                command.restriction().rangeStart(),
                command.restriction().rangeEnd(),
                command.restriction().reasonCode()
        );
        long revision = requireRevision(mapper.incrementRevision(command.targetUserId()));
        requireEventAppend(mapper.appendSnapshotEvent(
                command.targetUserId(),
                revision,
                command.traceId()
        ));
        return new CreateResult(MutationOutcome.CHANGED, restrictionId);
    }

    @Override
    public MutationOutcome revoke(
            UUID actorUserId,
            UUID targetUserId,
            UUID restrictionId,
            String traceId
    ) {
        if (!Boolean.TRUE.equals(mapper.actorAllowed(actorUserId))) {
            return MutationOutcome.DENIED;
        }
        if (mapper.lockTargetUser(targetUserId) == null) {
            return MutationOutcome.TARGET_NOT_FOUND;
        }
        Boolean active = mapper.restrictionActive(targetUserId, restrictionId);
        if (active == null) {
            return MutationOutcome.RESTRICTION_NOT_FOUND;
        }
        if (!active) {
            return MutationOutcome.UNCHANGED;
        }
        if (mapper.revokeRestriction(targetUserId, restrictionId) != 1) {
            throw new IllegalStateException("安全限制撤销写入失败");
        }
        long revision = requireRevision(mapper.incrementRevision(targetUserId));
        requireEventAppend(mapper.appendSnapshotEvent(targetUserId, revision, traceId));
        return MutationOutcome.CHANGED;
    }

    private static long requireRevision(Long revision) {
        if (revision == null) {
            throw new IllegalStateException("用户安全限制 revision 推进失败");
        }
        return revision;
    }

    private static void requireEventAppend(int changedRows) {
        if (changedRows != 1) {
            throw new IllegalStateException("用户安全限制 Outbox 写入失败");
        }
    }
}
