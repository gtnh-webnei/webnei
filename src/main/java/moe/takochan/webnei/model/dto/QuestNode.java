package moe.takochan.webnei.model.dto;

public record QuestNode(
        String questId,
        String name,
        String description,
        String visibility,
        int repeatTime,
        String iconItemVariantId,
        String iconAssetUrl,
        int posX,
        int posY,
        int sizeX,
        int sizeY) {
}
