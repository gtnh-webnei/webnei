package moe.takochan.webnei.fluid;

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
public class FluidDao {

    private final JdbcClient jdbc;
    private final AssetUrlBuilder assetUrlBuilder;

    public FluidDao(JdbcClient jdbc, AssetUrlBuilder assetUrlBuilder) {
        this.jdbc = jdbc;
        this.assetUrlBuilder = assetUrlBuilder;
    }

    public Optional<FluidDetail> findVariant(DatasetSummary dataset, String fluidVariantId) {
        return jdbc.sql("""
                        SELECT fv.fluid_variant_id, fv.fluid_id, fv.mod_id, fv.registry_name,
                               fv.unlocalized_name, fv.display_name, fv.gaseous, fv.density,
                               fv.temperature, fv.viscosity, fv.luminosity, fv.runtime_fluid_id,
                               fv.nbt_hash, fv.nbt_text, raw.chemical_expression, fv.asset_path
                        FROM v_fluid_variant_browser fv
                        JOIN fluid_variant raw
                          ON raw.dataset_id = fv.dataset_id
                         AND raw.fluid_variant_id = fv.fluid_variant_id
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
                rs.getString("chemical_expression"),
                assetUrl.apply(rs));
    }
}
