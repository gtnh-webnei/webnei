package moe.takochan.webnei.extras;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ExtrasDao {

    private static final int CONTAINER_PREVIEW_LIMIT = 50;

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public ExtrasDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<String> listOreDictNames(DatasetSummary dataset, String itemVariantId) {
        return jdbc.sql("""
                        SELECT DISTINCT od.ore_name
                        FROM ore_dictionary od
                        JOIN ingredient_entry ie
                          ON ie.dataset_id = od.dataset_id
                         AND ie.group_id = od.group_id
                        WHERE od.dataset_id = :datasetId
                          AND ie.item_variant_id = :itemVariantId
                        ORDER BY od.ore_name
                        """)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId)
                .query(String.class)
                .list();
    }

    public List<FluidContainerEntry> listContainersForItem(
            DatasetSummary dataset, String itemVariantId, Integer limit) {
        String sql = """
                SELECT fc.fluid_variant_id,
                       f.display_name AS fluid_display_name,
                       fc.container_item_variant_id,
                       cv.display_name AS container_display_name,
                       cv.asset_path AS container_asset_path,
                       cv.asset_sha256 AS container_asset_sha,
                       fc.empty_container_item_variant_id,
                       ev.display_name AS empty_display_name,
                       ev.asset_path AS empty_asset_path,
                       ev.asset_sha256 AS empty_asset_sha,
                       fc.amount
                FROM fluid_container fc
                JOIN fluid_variant fv
                  ON fv.dataset_id = fc.dataset_id AND fv.fluid_variant_id = fc.fluid_variant_id
                JOIN fluid f
                  ON f.dataset_id = fv.dataset_id AND f.fluid_id = fv.fluid_id
                LEFT JOIN v_item_variant_browser cv
                  ON cv.dataset_id = fc.dataset_id AND cv.item_variant_id = fc.container_item_variant_id
                LEFT JOIN v_item_variant_browser ev
                  ON ev.dataset_id = fc.dataset_id AND ev.item_variant_id = fc.empty_container_item_variant_id
                WHERE fc.dataset_id = :datasetId
                  AND (fc.container_item_variant_id = :itemVariantId
                       OR fc.empty_container_item_variant_id = :itemVariantId)
                ORDER BY fc.amount, fc.fluid_variant_id, fc.container_item_variant_id
                """ + (limit == null ? "" : " LIMIT :limit");
        var spec = jdbc.sql(sql)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId);
        if (limit != null) {
            spec.param("limit", limit);
        }
        return spec.query(containerMapper(dataset)).list();
    }

    public long countContainersForItem(DatasetSummary dataset, String itemVariantId) {
        return jdbc.sql("""
                        SELECT COUNT(*)
                        FROM fluid_container fc
                        WHERE fc.dataset_id = :datasetId
                          AND (fc.container_item_variant_id = :itemVariantId
                               OR fc.empty_container_item_variant_id = :itemVariantId)
                        """)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId)
                .query(Long.class)
                .single();
    }

    public List<FluidContainerEntry> listContainersForFluid(DatasetSummary dataset, String fluidVariantId) {
        String sql = """
                SELECT fc.fluid_variant_id,
                       f.display_name AS fluid_display_name,
                       fc.container_item_variant_id,
                       cv.display_name AS container_display_name,
                       cv.asset_path AS container_asset_path,
                       cv.asset_sha256 AS container_asset_sha,
                       fc.empty_container_item_variant_id,
                       ev.display_name AS empty_display_name,
                       ev.asset_path AS empty_asset_path,
                       ev.asset_sha256 AS empty_asset_sha,
                       fc.amount
                FROM fluid_container fc
                JOIN fluid_variant fv
                  ON fv.dataset_id = fc.dataset_id AND fv.fluid_variant_id = fc.fluid_variant_id
                JOIN fluid f
                  ON f.dataset_id = fv.dataset_id AND f.fluid_id = fv.fluid_id
                LEFT JOIN v_item_variant_browser cv
                  ON cv.dataset_id = fc.dataset_id AND cv.item_variant_id = fc.container_item_variant_id
                LEFT JOIN v_item_variant_browser ev
                  ON ev.dataset_id = fc.dataset_id AND ev.item_variant_id = fc.empty_container_item_variant_id
                WHERE fc.dataset_id = :datasetId
                  AND fc.fluid_variant_id = :fluidVariantId
                ORDER BY fc.amount, fc.container_item_variant_id
                """;
        return jdbc.sql(sql)
                .param("datasetId", dataset.datasetId())
                .param("fluidVariantId", fluidVariantId)
                .query(containerMapper(dataset))
                .list();
    }

    public List<AspectEntry> listAspectsForItem(DatasetSummary dataset, String itemVariantId) {
        String sql = """
                SELECT a.aspect_id,
                       a.name,
                       a.description,
                       a.primal,
                       ai.amount,
                       a.icon_item_variant_id,
                       iv.asset_path AS icon_asset_path,
                       iv.asset_sha256 AS icon_asset_sha
                FROM aspect_item ai
                JOIN aspect a
                  ON a.dataset_id = ai.dataset_id AND a.aspect_id = ai.aspect_id
                LEFT JOIN v_item_variant_browser iv
                  ON iv.dataset_id = a.dataset_id AND iv.item_variant_id = a.icon_item_variant_id
                WHERE ai.dataset_id = :datasetId
                  AND ai.item_variant_id = :itemVariantId
                ORDER BY ai.amount DESC, a.aspect_id
                """;
        return jdbc.sql(sql)
                .param("datasetId", dataset.datasetId())
                .param("itemVariantId", itemVariantId)
                .query(aspectMapper(dataset))
                .list();
    }

    public List<FluidBlockEntry> listBlocksForFluid(DatasetSummary dataset, String fluidVariantId) {
        return jdbc.sql("""
                        SELECT fb.block_item_variant_id,
                               iv.display_name AS block_display_name,
                               iv.asset_path AS block_asset_path,
                               iv.asset_sha256 AS block_asset_sha
                        FROM fluid_block fb
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = fb.dataset_id AND iv.item_variant_id = fb.block_item_variant_id
                        WHERE fb.dataset_id = :datasetId
                          AND fb.fluid_variant_id = :fluidVariantId
                        ORDER BY fb.block_item_variant_id
                        """)
                .param("datasetId", dataset.datasetId())
                .param("fluidVariantId", fluidVariantId)
                .query(blockMapper(dataset))
                .list();
    }

    public Map<String, Long> recipeCounts(DatasetSummary dataset, String targetDomain, String targetId) {
        return jdbc.sql("""
                        SELECT lookup_kind, COUNT(DISTINCT recipe_id) AS cnt
                        FROM recipe_lookup_index
                        WHERE dataset_id = :datasetId
                          AND target_domain = :targetDomain
                          AND target_id = :targetId
                        GROUP BY lookup_kind
                        """)
                .param("datasetId", dataset.datasetId())
                .param("targetDomain", targetDomain)
                .param("targetId", targetId)
                .query((rs, n) -> Map.entry(rs.getString("lookup_kind"), rs.getLong("cnt")))
                .list()
                .stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static int containerPreviewLimit() {
        return CONTAINER_PREVIEW_LIMIT;
    }

    private RowMapper<FluidContainerEntry> containerMapper(DatasetSummary dataset) {
        return (rs, n) -> new FluidContainerEntry(
                rs.getString("fluid_variant_id"),
                rs.getString("fluid_display_name"),
                rs.getString("container_item_variant_id"),
                rs.getString("container_display_name"),
                buildAssetUrl(dataset, rs, "container_asset_path", "container_asset_sha"),
                rs.getString("empty_container_item_variant_id"),
                rs.getString("empty_display_name"),
                buildAssetUrl(dataset, rs, "empty_asset_path", "empty_asset_sha"),
                rs.getInt("amount"));
    }

    private RowMapper<AspectEntry> aspectMapper(DatasetSummary dataset) {
        return (rs, n) -> new AspectEntry(
                rs.getString("aspect_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBoolean("primal"),
                rs.getInt("amount"),
                rs.getString("icon_item_variant_id"),
                buildAssetUrl(dataset, rs, "icon_asset_path", "icon_asset_sha"));
    }

    private RowMapper<FluidBlockEntry> blockMapper(DatasetSummary dataset) {
        return (rs, n) -> new FluidBlockEntry(
                rs.getString("block_item_variant_id"),
                rs.getString("block_display_name"),
                buildAssetUrl(dataset, rs, "block_asset_path", "block_asset_sha"));
    }

    private String buildAssetUrl(DatasetSummary dataset, ResultSet rs, String pathColumn, String shaColumn) {
        try {
            String path = rs.getString(pathColumn);
            String sha = rs.getString(shaColumn);
            return assetUrlBuilder.build(dataset, path, sha);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read asset columns", ex);
        }
    }
}
