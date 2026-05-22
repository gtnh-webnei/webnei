package moe.takochan.webnei.recipe.dto;

import tools.jackson.databind.JsonNode;

public record MetadataValueDto(
        String valueType,
        String valueText,
        JsonNode valueJson) {
}
