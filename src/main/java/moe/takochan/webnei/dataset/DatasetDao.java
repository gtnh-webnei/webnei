package moe.takochan.webnei.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import moe.takochan.webnei.common.PageRequest;
import moe.takochan.webnei.common.PageResponse;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetDao {

    private static final String DATASET_COLUMNS = """
            dataset_id, pack_slug, pack_version, variant, language, display_name,
            schema_version, exporter_version, minecraft_version, created_at, active_plugins
            """;

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    public DatasetDao(JdbcClient jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public List<DatasetSummary> listAll() {
        return jdbc.sql("SELECT " + DATASET_COLUMNS
                        + " FROM dataset ORDER BY pack_slug, pack_version, variant, language")
                .query(datasetMapper())
                .list();
    }

    public Optional<DatasetSummary> findById(String datasetId) {
        return jdbc.sql("SELECT " + DATASET_COLUMNS + " FROM dataset WHERE dataset_id = :id")
                .param("id", datasetId)
                .query(datasetMapper())
                .optional();
    }

    public List<ModSummary> listMods(String datasetId) {
        return jdbc.sql("""
                        SELECT mod_id, name, version, source_type, source_file_name, source_sha256, enabled
                        FROM mod
                        WHERE dataset_id = :id
                        ORDER BY name
                        """)
                .param("id", datasetId)
                .query((rs, n) -> new ModSummary(
                        rs.getString("mod_id"),
                        rs.getString("name"),
                        rs.getString("version"),
                        rs.getString("source_type"),
                        rs.getString("source_file_name"),
                        rs.getString("source_sha256"),
                        rs.getBoolean("enabled")))
                .list();
    }

    private static final Map<String, String> MOD_SORT_COLUMNS = Map.of(
            "modId", "mod_id",
            "name", "name",
            "version", "version",
            "sourceType", "source_type",
            "sourceFileName", "source_file_name",
            "enabled", "enabled");

    public PageResponse<ModSummary> listModsPage(
            String datasetId, String query, String sortField, boolean descending, PageRequest page) {
        String filter = (query == null || query.isBlank()) ? null : "%" + query.toLowerCase() + "%";
        String column = sortField == null ? "mod_id" : MOD_SORT_COLUMNS.getOrDefault(sortField, "mod_id");
        String direction = descending ? "DESC" : "ASC";

        long total = jdbc.sql("""
                        SELECT COUNT(*) FROM mod
                        WHERE dataset_id = :id
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(mod_id) LIKE CAST(:filter AS TEXT)
                               OR lower(name) LIKE CAST(:filter AS TEXT)
                               OR lower(version) LIKE CAST(:filter AS TEXT)
                               OR lower(source_file_name) LIKE CAST(:filter AS TEXT))
                        """)
                .param("id", datasetId)
                .param("filter", filter)
                .query(Long.class)
                .single();

        List<ModSummary> items = jdbc.sql("""
                        SELECT mod_id, name, version, source_type, source_file_name, source_sha256, enabled
                        FROM mod
                        WHERE dataset_id = :id
                          AND (CAST(:filter AS TEXT) IS NULL
                               OR lower(mod_id) LIKE CAST(:filter AS TEXT)
                               OR lower(name) LIKE CAST(:filter AS TEXT)
                               OR lower(version) LIKE CAST(:filter AS TEXT)
                               OR lower(source_file_name) LIKE CAST(:filter AS TEXT))
                        ORDER BY %s %s, mod_id ASC
                        OFFSET :offset LIMIT :limit
                        """.formatted(column, direction))
                .param("id", datasetId)
                .param("filter", filter)
                .param("offset", page.offset())
                .param("limit", page.size())
                .query((rs, n) -> new ModSummary(
                        rs.getString("mod_id"),
                        rs.getString("name"),
                        rs.getString("version"),
                        rs.getString("source_type"),
                        rs.getString("source_file_name"),
                        rs.getString("source_sha256"),
                        rs.getBoolean("enabled")))
                .list();
        return PageResponse.of(items, page, total);
    }

    private RowMapper<DatasetSummary> datasetMapper() {
        return (rs, n) -> new DatasetSummary(
                rs.getString("dataset_id"),
                rs.getString("pack_slug"),
                rs.getString("pack_version"),
                rs.getString("variant"),
                rs.getString("language"),
                rs.getString("display_name"),
                rs.getString("schema_version"),
                rs.getString("exporter_version"),
                rs.getString("minecraft_version"),
                readOffsetDateTime(rs, "created_at"),
                readPluginsJson(rs, "active_plugins"),
                null,
                null);
    }

    private static OffsetDateTime readOffsetDateTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, OffsetDateTime.class);
    }

    private List<String> readPluginsJson(ResultSet rs, String column) throws SQLException {
        String json = rs.getString(column);
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JacksonException ex) {
            throw new SQLException("Failed to parse active_plugins JSON for column " + column, ex);
        }
    }
}
