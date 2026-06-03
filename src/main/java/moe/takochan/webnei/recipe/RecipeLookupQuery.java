package moe.takochan.webnei.recipe;

public record RecipeLookupQuery(
        String target, String kind, String handlerId, String categoryId, String voltageTier) {

    public RecipeLookupQuery(String target, String kind) {
        this(target, kind, null, null, null);
    }

    public boolean isUsage() {
        return "usage".equalsIgnoreCase(kind);
    }

    public boolean hasHandlerFilter() {
        return handlerId != null && !handlerId.isBlank();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null && !categoryId.isBlank();
    }

    public boolean hasVoltageTierFilter() {
        return voltageTier != null && !voltageTier.isBlank();
    }
}
