package moe.takochan.webnei.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 把解析后的搜索词组装成一段"主键 IN (子查询)"条件，子查询命中搜索物化视图。
 *
 * <p>
 * 规范化（去色码、小写、跳首行、矿典聚合）已在物化视图各列固化，查询期只做 LIKE 子串匹配。
 * 每种 {@link SearchTermType} 映射到物化视图的一列；类型未被某个目录支持时（如流体没有
 * tooltip/矿典列），该词恒不命中。
 */
public final class CatalogSearchSupport {

    private static final String NO_MATCH = "1 = 0";

    private final String viewName;
    private final String idColumn;
    private final Map<SearchTermType, String> columnByType;

    /**
     * @param viewName 搜索物化视图名（如 {@code mv_item_search}）
     * @param idColumn 物化视图与展示视图对应的主键列（如 {@code item_variant_id}）
     * @param columnByType 各搜索类型对应的规范化列名；缺失的类型视为该目录不支持
     */
    public CatalogSearchSupport(String viewName, String idColumn, Map<SearchTermType, String> columnByType) {
        this.viewName = viewName;
        this.idColumn = idColumn;
        this.columnByType = columnByType;
    }

    /**
     * 若存在搜索词，向 {@code query} 追加一段 {@code idColumn IN (SELECT idColumn FROM view WHERE ...)}。
     * 分组之间为 OR，组内为 AND，取反包 NOT。LIKE 模式作为绑定参数传入，避免注入。
     * 无搜索词时不改动 query。
     */
    public <T> void apply(QueryWrapper<T> query, String targetIdColumn, List<List<SearchTerm>> searchGroups) {
        if (searchGroups.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(targetIdColumn).append(" IN (SELECT ").append(idColumn)
                .append(" FROM ").append(viewName).append(" WHERE ");
        List<Object> values = new ArrayList<>();
        appendGroups(sql, values, searchGroups);
        sql.append(')');
        query.apply(sql.toString(), values.toArray());
    }

    private void appendGroups(StringBuilder sql, List<Object> values, List<List<SearchTerm>> groups) {
        for (int g = 0; g < groups.size(); g++) {
            if (g > 0) {
                sql.append(" OR ");
            }
            sql.append('(');
            List<SearchTerm> group = groups.get(g);
            for (int t = 0; t < group.size(); t++) {
                if (t > 0) {
                    sql.append(" AND ");
                }
                sql.append(condition(group.get(t), values));
            }
            sql.append(')');
        }
    }

    private String condition(SearchTerm term, List<Object> values) {
        String column = columnByType.get(term.getType());
        if (column == null) {
            return NO_MATCH;
        }
        String match = column + " LIKE {" + values.size() + "} ESCAPE '!'";
        values.add(term.getPattern());
        return term.isNegated() ? "NOT (" + match + ")" : match;
    }
}
