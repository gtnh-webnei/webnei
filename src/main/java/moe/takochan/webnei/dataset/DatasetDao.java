package moe.takochan.webnei.dataset;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetDao {

    private final JdbcClient jdbc;

    public DatasetDao(JdbcClient jdbc) {
        this.jdbc = jdbc;
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
}
