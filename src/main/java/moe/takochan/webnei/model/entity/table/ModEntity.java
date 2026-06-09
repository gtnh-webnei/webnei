package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ModEntity.ModId.class)
@Table(name = "mod")
public class ModEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_file_name", nullable = false)
    private String sourceFileName;

    @Column(name = "source_sha256", nullable = false)
    private String sourceSha256;

    @Column(nullable = false)
    private boolean enabled;

    protected ModEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getModId() { return modId; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getSourceType() { return sourceType; }
    public String getSourceFileName() { return sourceFileName; }
    public String getSourceSha256() { return sourceSha256; }
    public boolean isEnabled() { return enabled; }

    public static class ModId {
        private String datasetId;
        private String modId;

        public ModId() {}
        public ModId(String datasetId, String modId) { this.datasetId = datasetId; this.modId = modId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModId other)) return false;
            return datasetId.equals(other.datasetId) && modId.equals(other.modId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + modId.hashCode(); }
    }
}
