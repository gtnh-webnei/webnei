package moe.takochan.webnei.model.dto;

import java.util.List;

public record MobDetail(
        MobSummary summary,
        MobInfo info,
        List<MobDropRow> drops) {

    public record MobInfo(
            boolean allowedInPeaceful,
            boolean soulVialUsable,
            boolean allowedInfernal,
            boolean alwaysInfernal) {
    }
}
