package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "dataset")
public class DatasetEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Column(name = "pack_slug", nullable = false)
    private String packSlug;

    @Column(name = "pack_version", nullable = false)
    private String packVersion;

    @Column(nullable = false)
    private String variant;

    @Column(nullable = false)
    private String language;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    @Column(name = "exporter_version", nullable = false)
    private String exporterVersion;

    @Column(name = "minecraft_version", nullable = false)
    private String minecraftVersion;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "active_plugins", columnDefinition = "jsonb", nullable = false)
    private String activePlugins;

    protected DatasetEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getPackSlug() { return packSlug; }
    public String getPackVersion() { return packVersion; }
    public String getVariant() { return variant; }
    public String getLanguage() { return language; }
    public String getDisplayName() { return displayName; }
    public String getSchemaVersion() { return schemaVersion; }
    public String getExporterVersion() { return exporterVersion; }
    public String getMinecraftVersion() { return minecraftVersion; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getActivePlugins() { return activePlugins; }
}
