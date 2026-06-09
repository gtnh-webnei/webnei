package moe.takochan.webnei.mob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(MobSearchDocumentEntity.MobSearchDocumentId.class)
@Table(name = "mob_search_document")
public class MobSearchDocumentEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mob_variant_id", nullable = false)
    private String mobVariantId;

    @Column(name = "mob_id", nullable = false)
    private String mobId;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected MobSearchDocumentEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getMobVariantId() { return mobVariantId; }
    public String getMobId() { return mobId; }
    public String getModId() { return modId; }
    public String getDisplayName() { return displayName; }
    public String getEntityName() { return entityName; }
    public String getSearchText() { return searchText; }

    public static class MobSearchDocumentId {
        private String datasetId;
        private String mobVariantId;

        public MobSearchDocumentId() {}
        public MobSearchDocumentId(String datasetId, String mobVariantId) {
            this.datasetId = datasetId;
            this.mobVariantId = mobVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MobSearchDocumentId other)) return false;
            return datasetId.equals(other.datasetId) && mobVariantId.equals(other.mobVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + mobVariantId.hashCode(); }
    }
}
