package moe.takochan.webnei.model.dto;

import java.util.List;

public record HandlerBreakdownDto(
        String handlerId,
        String displayName,
        String iconAssetUrl,
        long count,
        List<CategoryBreakdownDto> categories) {
}
