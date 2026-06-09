package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(NeiTextureExportEntity.NeiTextureExportId.class)
@Table(name = "nei_texture_export")
public class NeiTextureExportEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(nullable = false)
    private String resource;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String path;

    @Column(name = "exported_path", nullable = false)
    private String exportedPath;

    protected NeiTextureExportEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getResource() { return resource; }
    public String getDomain() { return domain; }
    public String getPath() { return path; }
    public String getExportedPath() { return exportedPath; }

    public static class NeiTextureExportId {
        private String datasetId;
        private String resource;

        public NeiTextureExportId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NeiTextureExportId other)) return false;
            return datasetId.equals(other.datasetId) && resource.equals(other.resource);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + resource.hashCode(); }
    }
}
