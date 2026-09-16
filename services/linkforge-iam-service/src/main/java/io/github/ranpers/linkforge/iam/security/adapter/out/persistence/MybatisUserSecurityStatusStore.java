package io.github.ranpers.linkforge.iam.security.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.security.application.port.out.UserSecurityStatusStore;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class MybatisUserSecurityStatusStore implements UserSecurityStatusStore {

    private final UserSecurityStatusMapper mapper;
    private final LinkSecurityRestrictionMapper restrictionMapper;

    public MybatisUserSecurityStatusStore(
            UserSecurityStatusMapper mapper,
            LinkSecurityRestrictionMapper restrictionMapper
    ) {
        this.mapper = mapper;
        this.restrictionMapper = restrictionMapper;
    }

    @Override
    public ChangeOutcome change(
            UUID actorUserId,
            UUID targetUserId,
            boolean suspended,
            ControlEventTraceId traceId
    ) {
        Integer result = mapper.change(actorUserId, targetUserId, suspended);
        ChangeOutcome outcome = switch (result == null ? 0 : result) {
            case 4 -> ChangeOutcome.CHANGED;
            case 3 -> ChangeOutcome.UNCHANGED;
            case 2 -> ChangeOutcome.LIFECYCLE_CONFLICT;
            case 1 -> ChangeOutcome.TARGET_NOT_FOUND;
            default -> ChangeOutcome.DENIED;
        };
        if (outcome != ChangeOutcome.CHANGED && outcome != ChangeOutcome.UNCHANGED) {
            return outcome;
        }

        int changedRestrictions = suspended
                ? mapper.activateAccountSuspensionRestriction(targetUserId)
                : mapper.revokeAccountSuspensionRestriction(targetUserId);
        if (changedRestrictions < 0 || changedRestrictions > 1) {
            throw new IllegalStateException("用户冻结对应的短链安全限制写入结果异常");
        }
        if (changedRestrictions == 0) {
            return outcome;
        }

        long revision = requireRevision(restrictionMapper.incrementRevision(targetUserId));
        requireEventAppend(restrictionMapper.appendSnapshotEvent(
                targetUserId,
                revision,
                traceId == null ? null : traceId.value()
        ));
        return ChangeOutcome.CHANGED;
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
