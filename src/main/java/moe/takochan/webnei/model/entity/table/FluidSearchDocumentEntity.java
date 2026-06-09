package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidSearchDocumentEntity.FluidSearchDocumentId.class)
@Table(name = "fluid_search_document")
public class FluidSearchDocumentEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Column(name = "fluid_id", nullable = false)
    private String fluidId;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected FluidSearchDocumentEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getFluidId() { return fluidId; }
    public String getModId() { return modId; }
    public String getDisplayName() { return displayName; }
    public String getSearchText() { return searchText; }

    public static class FluidSearchDocumentId {
        private String datasetId;
        private String fluidVariantId;

        public FluidSearchDocumentId() {}
        public FluidSearchDocumentId(String datasetId, String fluidVariantId) {
            this.datasetId = datasetId;
            this.fluidVariantId = fluidVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidSearchDocumentId other)) return false;
            return datasetId.equals(other.datasetId) && fluidVariantId.equals(other.fluidVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + fluidVariantId.hashCode(); }
    }
}
