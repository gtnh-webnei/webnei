package moe.takochan.webnei.model.dto;

public record CategoryMachineDto(
        String itemVariantId,
        String displayName,
        String assetUrl,
        String role,
        int displayOrder) {
}
