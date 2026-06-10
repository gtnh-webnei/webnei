package moe.takochan.webnei.model.dto;

public record CategoryApplicableItemDto(
        String itemVariantId,
        String displayName,
        String assetUrl,
        String role,
        int displayOrder) {
}
