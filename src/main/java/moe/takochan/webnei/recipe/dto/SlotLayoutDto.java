package moe.takochan.webnei.recipe.dto;

public record SlotLayoutDto(
        String role,
        int slotIndex,
        int x,
        int y,
        int width,
        int height,
        String slotStyle) {
}
