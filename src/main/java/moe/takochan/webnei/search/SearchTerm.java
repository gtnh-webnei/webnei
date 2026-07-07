package moe.takochan.webnei.search;

import lombok.Getter;

@Getter
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
}
