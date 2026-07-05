package moe.takochan.webnei.catalog.search;

public final class SearchTerm {

    private final SearchTermType type;
    private final String value;
    private final String pattern;
    private final boolean negated;

    public SearchTerm(SearchTermType type, String value, String pattern, boolean negated) {
        this.type = type;
        this.value = value;
        this.pattern = pattern;
        this.negated = negated;
    }

    public SearchTermType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isNegated() {
        return negated;
    }

    public boolean isText() {
        return type == SearchTermType.TEXT;
    }

    public boolean isMod() {
        return type == SearchTermType.MOD;
    }

    public boolean isTooltip() {
        return type == SearchTermType.TOOLTIP;
    }

    public boolean isOre() {
        return type == SearchTermType.ORE_DICTIONARY;
    }

    public boolean isIdentifier() {
        return type == SearchTermType.IDENTIFIER;
    }
}
