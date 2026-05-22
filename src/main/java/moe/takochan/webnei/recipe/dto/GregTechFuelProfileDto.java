package moe.takochan.webnei.recipe.dto;

public record GregTechFuelProfileDto(
        String categoryId,
        String machineKind,
        String displayName,
        Integer baseEfficiencyPercent,
        String tierEfficiencyFormula,
        String consumptionUnit) {
}
