package moe.takochan.webnei.catalog.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class SearchQueryParser {

    private static final String PREFIXES = "@#$&";

    public List<List<SearchTerm>> parse(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        List<List<SearchTerm>> groups = new ArrayList<>();
        List<SearchTerm> currentGroup = new ArrayList<>();
        int index = 0;
        while (index < query.length()) {
            index = skipSpaces(query, index);
            if (index >= query.length()) {
                break;
            }

            if (query.charAt(index) == '|') {
                addGroup(groups, currentGroup);
                currentGroup = new ArrayList<>();
                index++;
                continue;
            }

            Token token = readToken(query, index);
            SearchTerm term = parseToken(token.value());
            if (!term.getValue().isBlank()) {
                currentGroup.add(term);
            }
            index = token.end();
        }
        addGroup(groups, currentGroup);
        return groups;
    }

    private static int skipSpaces(String query, int index) {
        while (index < query.length() && Character.isWhitespace(query.charAt(index))) {
            index++;
        }
        return index;
    }

    private static void addGroup(List<List<SearchTerm>> groups, List<SearchTerm> group) {
        if (!group.isEmpty()) {
            groups.add(List.copyOf(group));
        }
    }

    private static Token readToken(String query, int start) {
        boolean insideQuotes = false;
        int index = start;
        while (index < query.length()) {
            char c = query.charAt(index);
            if (c == '"' && !isEscaped(query, index)) {
                insideQuotes = !insideQuotes;
            } else if (!insideQuotes && (Character.isWhitespace(c) || c == '|')) {
                break;
            }
            index++;
        }
        return new Token(query.substring(start, index), index);
    }

    private static boolean isEscaped(String value, int index) {
        int slashCount = 0;
        for (int i = index - 1; i >= 0 && value.charAt(i) == '\\'; i--) {
            slashCount++;
        }
        return slashCount % 2 == 1;
    }

    private SearchTerm parseToken(String token) {
        SearchTermType type = SearchTermType.TEXT;
        boolean negated = false;
        String value = token.trim();

        if (value.length() > 1 && (value.charAt(0) == '-' || value.charAt(0) == '!')) {
            negated = true;
            value = value.substring(1);
        }
        if (value.length() > 1 && PREFIXES.indexOf(value.charAt(0)) >= 0) {
            type = typeForPrefix(value.charAt(0));
            value = value.substring(1);
        }

        value = unquote(value);
        return new SearchTerm(type, value, containsPattern(value), negated);
    }

    private static SearchTermType typeForPrefix(char prefix) {
        return switch (prefix) {
            case '@' -> SearchTermType.MOD;
            case '#' -> SearchTermType.TOOLTIP;
            case '$' -> SearchTermType.ORE_DICTIONARY;
            case '&' -> SearchTermType.IDENTIFIER;
            default -> SearchTermType.TEXT;
        };
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1).replace("\\\"", "\"");
        }
        return value;
    }

    private static String containsPattern(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        StringBuilder escaped = new StringBuilder(normalized.length() + 2);
        escaped.append('%');
        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            if (c == '!' || c == '%' || c == '_') {
                escaped.append('!');
            }
            escaped.append(c);
        }
        escaped.append('%');
        return escaped.toString();
    }

    private record Token(String value, int end) {}
}
