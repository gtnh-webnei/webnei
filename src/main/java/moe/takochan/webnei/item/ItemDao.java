package moe.takochan.webnei.item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ItemDao {

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public ItemDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
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
                          raw.chemical_expression,
                          iv.display_name,
                          iv.tooltip_text,
                          iv.asset_path,
                          iv.asset_sha256,
                          iv.asset_width,
                          iv.asset_height
                        FROM v_item_variant_browser iv
                        JOIN item_variant raw
                          ON raw.dataset_id = iv.dataset_id
                         AND raw.item_variant_id = iv.item_variant_id
                        WHERE iv.dataset_id = :datasetId
                          AND iv.item_variant_id = :itemVariantId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId)
                .query(detailMapper(dataset))
                .optional();
    }

    public List<ModOptionDto> listMods(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT used.mod_id, COALESCE(m.name, used.mod_id) AS name
                        FROM (
                            SELECT DISTINCT mod_id
                            FROM item
                            WHERE dataset_id = :datasetId AND mod_id <> ''
                        ) used
                        LEFT JOIN mod m
                          ON m.dataset_id = :datasetId AND m.mod_id = used.mod_id
                        ORDER BY name, used.mod_id
                        """)
                .param("datasetId", dataset.datasetId())
                .query((rs, n) -> new ModOptionDto(rs.getString("mod_id"), rs.getString("name")))
                .list();
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
                rs.getString("chemical_expression"),
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
