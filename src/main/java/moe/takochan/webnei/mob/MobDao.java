package moe.takochan.webnei.mob;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;
import moe.takochan.webnei.dataset.DatasetSummary;
import moe.takochan.webnei.mob.dto.MobDetail;
import moe.takochan.webnei.mob.dto.MobDropRow;
import moe.takochan.webnei.mob.dto.MobSummary;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class MobDao {

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public MobDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public PageResponse<MobSummary> listMobs(
            DatasetSummary dataset, String q, String modId, PageRequest page) {
        String filter = (q == null || q.isBlank()) ? null : "%" + q.toLowerCase() + "%";
        long total = jdbc.sql("""
                        SELECT COUNT(*) FROM v_mob_variant_browser
                        WHERE dataset_id = :datasetId
                          AND (CAST(:modId AS TEXT) IS NULL OR mod_id = CAST(:modId AS TEXT))
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(display_name) LIKE CAST(:filter AS TEXT)
                               OR lower(entity_name) LIKE CAST(:filter AS TEXT))
                        """)
                .param("datasetId", dataset.datasetId())
                .param("modId", modId)
                .param("filter", filter)
                .query(Long.class)
                .single();

        List<MobSummary> items = jdbc.sql("""
                        SELECT mob_variant_id, mob_id, mod_id, entity_name, display_name,
                               width, height, max_health, armor,
                               immune_to_fire, leashable, asset_path
                        FROM v_mob_variant_browser
                        WHERE dataset_id = :datasetId
                          AND (CAST(:modId AS TEXT) IS NULL OR mod_id = CAST(:modId AS TEXT))
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(display_name) LIKE CAST(:filter AS TEXT)
                               OR lower(entity_name) LIKE CAST(:filter AS TEXT))
                        ORDER BY display_name, mob_variant_id
                        LIMIT :size OFFSET :offset
                        """)
                .param("datasetId", dataset.datasetId())
                .param("modId", modId)
                .param("filter", filter)
                .param("size", page.size())
                .param("offset", page.offset())
                .query((rs, n) -> mapSummary(dataset, rs))
                .list();
        return PageResponse.of(items, page, total);
    }

    public List<String> listMods(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT DISTINCT mod_id FROM v_mob_variant_browser
                        WHERE dataset_id = :datasetId AND mod_id <> ''
                        ORDER BY mod_id
                        """)
                .param("datasetId", dataset.datasetId())
                .query(String.class)
                .list();
    }

    public Optional<MobDetail> findMob(DatasetSummary dataset, String mobVariantId) {
        Optional<MobSummary> summary = jdbc.sql("""
                        SELECT mob_variant_id, mob_id, mod_id, entity_name, display_name,
                               width, height, max_health, armor,
                               immune_to_fire, leashable, asset_path
                        FROM v_mob_variant_browser
                        WHERE dataset_id = :datasetId AND mob_variant_id = :id
                        """)
                .param("datasetId", dataset.datasetId())
                .param("id", mobVariantId)
                .query((rs, n) -> mapSummary(dataset, rs))
                .optional();
        if (summary.isEmpty()) return Optional.empty();

        MobDetail.MobInfo info = jdbc.sql("""
                        SELECT allowed_in_peaceful, soul_vial_usable, allowed_infernal, always_infernal
                        FROM mob_info
                        WHERE dataset_id = :datasetId AND mob_variant_id = :id
                        """)
                .param("datasetId", dataset.datasetId())
                .param("id", mobVariantId)
                .query((rs, n) -> new MobDetail.MobInfo(
                        rs.getBoolean("allowed_in_peaceful"),
                        rs.getBoolean("soul_vial_usable"),
                        rs.getBoolean("allowed_infernal"),
                        rs.getBoolean("always_infernal")))
                .optional()
                .orElse(new MobDetail.MobInfo(false, false, false, false));

        // 掉落表:概率降序 → 列表顺序;item 信息从 v_item_variant_browser 取 displayName + icon
        List<MobDropRow> drops = jdbc.sql("""
                        SELECT d.drop_type, d.list_index, d.item_variant_id,
                               d.stack_size, d.probability, d.lootable, d.player_only,
                               iv.display_name, iv.asset_path, iv.asset_sha256
                        FROM mob_drop d
                        LEFT JOIN v_item_variant_browser iv
                          ON iv.dataset_id = d.dataset_id AND iv.item_variant_id = d.item_variant_id
                        WHERE d.dataset_id = :datasetId AND d.mob_variant_id = :id
                        ORDER BY d.probability DESC, d.list_index
                        """)
                .param("datasetId", dataset.datasetId())
                .param("id", mobVariantId)
                .query((rs, n) -> new MobDropRow(
                        rs.getString("drop_type"),
                        rs.getInt("list_index"),
                        rs.getString("item_variant_id"),
                        rs.getString("display_name"),
                        assetUrlBuilder.build(dataset, rs.getString("asset_path"), rs.getString("asset_sha256")),
                        rs.getInt("stack_size"),
                        rs.getDouble("probability"),
                        rs.getBoolean("lootable"),
                        rs.getBoolean("player_only")))
                .list();

        return Optional.of(new MobDetail(summary.get(), info, drops));
    }

    private MobSummary mapSummary(DatasetSummary dataset, ResultSet rs) throws SQLException {
        // v_mob_variant_browser 没有 sha256 列,asset url 不带 ?v= 缓存破坏 — mob 渲染图量少,可接受
        return new MobSummary(
                rs.getString("mob_variant_id"),
                rs.getString("mob_id"),
                rs.getString("mod_id"),
                rs.getString("entity_name"),
                rs.getString("display_name"),
                rs.getDouble("width"),
                rs.getDouble("height"),
                rs.getDouble("max_health"),
                rs.getInt("armor"),
                rs.getBoolean("immune_to_fire"),
                rs.getBoolean("leashable"),
                assetUrlBuilder.build(dataset, rs.getString("asset_path"), null));
    }
}
