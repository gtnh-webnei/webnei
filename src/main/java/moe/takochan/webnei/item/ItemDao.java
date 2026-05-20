package moe.takochan.webnei.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ItemDao {

    private static final String PANEL_COLUMNS = """
            iv.item_variant_id,
            iv.item_id,
            iv.mod_id,
            iv.registry_name,
            iv.damage,
            iv.nbt_hash,
            iv.display_name,
            iv.asset_path,
            iv.asset_sha256,
            p.panel_index
            """;

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public ItemDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<NeiPanelEntryDto> listPanel(DatasetSummary dataset, ItemQuery query, PageRequest page) {
        String sql = """
                SELECT %s
                FROM nei_item_panel_entry p
                JOIN v_item_variant_browser iv
                  ON iv.dataset_id = p.dataset_id
                 AND iv.item_variant_id = p.item_variant_id
                WHERE p.dataset_id = :datasetId
                """.formatted(PANEL_COLUMNS)
                + buildFilterClause(query)
                + " ORDER BY p.panel_index"
                + " OFFSET :offset LIMIT :limit";

        var spec = jdbc.sql(sql)
                .param("datasetId", dataset.datasetId())
                .param("offset", page.offset())
                .param("limit", page.size());
        bindFilterParams(spec, query);
        return spec.query(panelMapper(dataset)).list();
    }

    public long countPanel(DatasetSummary dataset, ItemQuery query) {
        String sql = """
                SELECT COUNT(*)
                FROM nei_item_panel_entry p
                JOIN v_item_variant_browser iv
                  ON iv.dataset_id = p.dataset_id
                 AND iv.item_variant_id = p.item_variant_id
                WHERE p.dataset_id = :datasetId
                """ + buildFilterClause(query);

        var spec = jdbc.sql(sql).param("datasetId", dataset.datasetId());
        bindFilterParams(spec, query);
        return spec.query(Long.class).single();
    }

    public Optional<ItemDetailDto> findVariant(DatasetSummary dataset, String itemVariantId) {
        return jdbc.sql("""
                        SELECT
                          iv.item_variant_id,
                          iv.item_id,
                          iv.mod_id,
                          iv.registry_name,
                          iv.unlocalized_name,
                          iv.max_stack_size,
                          iv.max_damage,
                          iv.damage,
                          iv.nbt_hash,
                          iv.nbt_text,
                          iv.display_name,
                          iv.tooltip_text,
                          iv.asset_path,
                          iv.asset_sha256,
                          iv.asset_width,
                          iv.asset_height
                        FROM v_item_variant_browser iv
                        WHERE iv.dataset_id = :datasetId
                          AND iv.item_variant_id = :itemVariantId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId)
                .query(detailMapper(dataset))
                .optional();
    }

    public List<String> listModIds(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT DISTINCT mod_id
                        FROM item
                        WHERE dataset_id = :datasetId
                        ORDER BY mod_id
                        """)
                .param("datasetId", dataset.datasetId())
                .query(String.class)
                .list();
    }

    private static String buildFilterClause(ItemQuery query) {
        StringBuilder sb = new StringBuilder();
        if (query.modId() != null && !query.modId().isBlank()) {
            sb.append(" AND iv.mod_id = :modId");
        }
        if (query.q() != null && !query.q().isBlank()) {
            sb.append(" AND (iv.display_name ILIKE :q"
                    + " OR iv.registry_name ILIKE :q"
                    + " OR iv.item_id ILIKE :q)");
        }
        return sb.toString();
    }

    private static void bindFilterParams(JdbcClient.StatementSpec spec, ItemQuery query) {
        if (query.modId() != null && !query.modId().isBlank()) {
            spec.param("modId", query.modId());
        }
        if (query.q() != null && !query.q().isBlank()) {
            spec.param("q", "%" + query.q() + "%");
        }
    }

    private RowMapper<NeiPanelEntryDto> panelMapper(DatasetSummary dataset) {
        Function<ResultSet, String> assetUrl = mapAssetUrl(dataset);
        return (rs, n) -> new NeiPanelEntryDto(
                rs.getString("item_variant_id"),
                rs.getString("item_id"),
                rs.getString("mod_id"),
                rs.getString("registry_name"),
                rs.getInt("damage"),
                rs.getString("nbt_hash"),
                rs.getString("display_name"),
                assetUrl.apply(rs),
                rs.getInt("panel_index"));
    }

    private RowMapper<ItemDetailDto> detailMapper(DatasetSummary dataset) {
        Function<ResultSet, String> assetUrl = mapAssetUrl(dataset);
        return (rs, n) -> new ItemDetailDto(
                rs.getString("item_variant_id"),
                rs.getString("item_id"),
                rs.getString("mod_id"),
                rs.getString("registry_name"),
                rs.getString("unlocalized_name"),
                rs.getInt("max_stack_size"),
                rs.getInt("max_damage"),
                rs.getInt("damage"),
                rs.getString("nbt_hash"),
                rs.getString("nbt_text"),
                rs.getString("display_name"),
                rs.getString("tooltip_text"),
                assetUrl.apply(rs),
                nullableInt(rs, "asset_width"),
                nullableInt(rs, "asset_height"));
    }

    private Function<ResultSet, String> mapAssetUrl(DatasetSummary dataset) {
        return rs -> {
            try {
                String path = rs.getString("asset_path");
                String sha = rs.getString("asset_sha256");
                return assetUrlBuilder.build(dataset, path, sha);
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to read asset columns", ex);
            }
        };
    }

    private static Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }
}
