package moe.takochan.webnei.fluid;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import moe.takochan.webnei.asset.AssetUrlBuilder;
import moe.takochan.webnei.common.ModOptionDto;
import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.dataset.DatasetSummary;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class FluidDao {

    private static final String SUMMARY_COLUMNS = """
            fv.fluid_variant_id,
            fv.fluid_id,
            fv.mod_id,
            fv.registry_name,
            fv.display_name,
            fv.gaseous,
            fv.density,
            fv.temperature,
            fv.viscosity,
            fv.luminosity,
            fv.nbt_hash,
            fv.asset_path
            """;

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public FluidDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public List<FluidSummary> list(DatasetSummary dataset, FluidQuery query, PageRequest page) {
        String sql = """
                SELECT %s
                FROM v_fluid_variant_browser fv
                WHERE fv.dataset_id = :datasetId
                """.formatted(SUMMARY_COLUMNS)
                + buildFilterClause(query)
                + " ORDER BY fv.mod_id, fv.display_name, fv.fluid_variant_id"
                + " OFFSET :offset LIMIT :limit";

        var spec = jdbc.sql(sql)
                .param("datasetId", dataset.datasetId())
                .param("offset", page.offset())
                .param("limit", page.size());
        bindFilterParams(spec, query);
        return spec.query(summaryMapper(dataset)).list();
    }

    public long count(DatasetSummary dataset, FluidQuery query) {
        String sql = """
                SELECT COUNT(*)
                FROM v_fluid_variant_browser fv
                WHERE fv.dataset_id = :datasetId
                """ + buildFilterClause(query);
        var spec = jdbc.sql(sql).param("datasetId", dataset.datasetId());
        bindFilterParams(spec, query);
        return spec.query(Long.class).single();
    }

    public Optional<FluidDetail> findVariant(DatasetSummary dataset, String fluidVariantId) {
        return jdbc.sql("""
                        SELECT fv.fluid_variant_id, fv.fluid_id, fv.mod_id, fv.registry_name,
                               fv.unlocalized_name, fv.display_name, fv.gaseous, fv.density,
                               fv.temperature, fv.viscosity, fv.luminosity, fv.runtime_fluid_id,
                               fv.nbt_hash, fv.nbt_text, fv.asset_path
                        FROM v_fluid_variant_browser fv
                        WHERE fv.dataset_id = :datasetId
                          AND fv.fluid_variant_id = :fluidVariantId
                        """)
                .param("datasetId", dataset.datasetId())
                .param("fluidVariantId", fluidVariantId)
                .query(detailMapper(dataset))
                .optional();
    }

    public List<ModOptionDto> listMods(DatasetSummary dataset) {
        return jdbc.sql("""
                        SELECT used.mod_id, COALESCE(m.name, used.mod_id) AS name
                        FROM (
                            SELECT DISTINCT mod_id
                            FROM fluid
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

    private static String buildFilterClause(FluidQuery query) {
        StringBuilder sb = new StringBuilder();
        if (query.modId() != null && !query.modId().isBlank()) {
            sb.append(" AND fv.mod_id = :modId");
        }
        if (query.q() != null && !query.q().isBlank()) {
            sb.append(" AND (fv.display_name ILIKE :q"
                    + " OR fv.registry_name ILIKE :q"
                    + " OR fv.fluid_id ILIKE :q)");
        }
        return sb.toString();
    }

    private static void bindFilterParams(JdbcClient.StatementSpec spec, FluidQuery query) {
        if (query.modId() != null && !query.modId().isBlank()) {
            spec.param("modId", query.modId());
        }
        if (query.q() != null && !query.q().isBlank()) {
            spec.param("q", "%" + query.q() + "%");
        }
    }

    private RowMapper<FluidSummary> summaryMapper(DatasetSummary dataset) {
        Function<ResultSet, String> assetUrl = rs -> {
            try {
                return assetUrlBuilder.build(dataset, rs.getString("asset_path"), null);
            } catch (SQLException ex) {
                throw new IllegalStateException("read asset_path", ex);
            }
        };
        return (rs, n) -> new FluidSummary(
                rs.getString("fluid_variant_id"),
                rs.getString("fluid_id"),
                rs.getString("mod_id"),
                rs.getString("registry_name"),
                rs.getString("display_name"),
                rs.getBoolean("gaseous"),
                rs.getInt("density"),
                rs.getInt("temperature"),
                rs.getInt("viscosity"),
                rs.getInt("luminosity"),
                rs.getString("nbt_hash"),
                assetUrl.apply(rs));
    }

    private RowMapper<FluidDetail> detailMapper(DatasetSummary dataset) {
        Function<ResultSet, String> assetUrl = rs -> {
            try {
                return assetUrlBuilder.build(dataset, rs.getString("asset_path"), null);
            } catch (SQLException ex) {
                throw new IllegalStateException("read asset_path", ex);
            }
        };
        return (rs, n) -> new FluidDetail(
                rs.getString("fluid_variant_id"),
                rs.getString("fluid_id"),
                rs.getString("mod_id"),
                rs.getString("registry_name"),
                rs.getString("unlocalized_name"),
                rs.getString("display_name"),
                rs.getBoolean("gaseous"),
                rs.getInt("density"),
                rs.getInt("temperature"),
                rs.getInt("viscosity"),
                rs.getInt("luminosity"),
                rs.getInt("runtime_fluid_id"),
                rs.getString("nbt_hash"),
                rs.getString("nbt_text"),
                assetUrl.apply(rs));
    }
}
