package moe.takochan.webnei.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 把解析后的搜索词组装成一段“dataset + 主键 IN (子查询)”条件，子查询命中搜索视图。
 *
 * <p>
 * 规范化（去色码、小写、跳首行、矿典聚合）已在搜索视图各列固化，查询期只做 LIKE 子串匹配。
 * 每种 {@link SearchTermType} 映射到搜索视图的一列；类型未被某个目录支持时，该词恒不命中。
 */
public final class CatalogSearchSupport {

    private static final String DATASET_COLUMN = "dataset_id";
    private static final String NO_MATCH = "1 = 0";

    private final String viewName;
    private final String idColumn;
    private final Map<SearchTermType, String> columnByType;

    public CatalogSearchSupport(String viewName, String idColumn, Map<SearchTermType, String> columnByType) {
        this.viewName = viewName;
        this.idColumn = idColumn;
        this.columnByType = columnByType;
    }

    /**
     * 若存在搜索词，按 dataset 与条目 ID 同时匹配搜索视图。分组之间为 OR，组内为 AND，取反包 NOT。
     * LIKE 模式作为绑定参数传入；无搜索词时不修改 query。
     */
    public <T> void apply(
            QueryWrapper<T> query,
            String targetIdColumn,
            List<List<SearchTerm>> searchGroups) {
        if (searchGroups.isEmpty()) {
            return;
        }
        StringBuilder sql = new StringBuilder();
        sql.append('(')
                .append(DATASET_COLUMN)
                .append(", ")
                .append(targetIdColumn)
                .append(") IN (SELECT dataset_id, ")
                .append(idColumn)
                .append(" FROM ")
                .append(viewName)
                .append(" WHERE ");
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
