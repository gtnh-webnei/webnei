package moe.takochan.webnei.extras;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeLookupCountEntity.RecipeLookupCountId.class)
@Table(name = "v_recipe_lookup_count")
public class RecipeLookupCountEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "target_domain", nullable = false)
    private String targetDomain;

    @Id
    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Id
    @Column(name = "lookup_kind", nullable = false)
    private String lookupKind;

    @Column(name = "recipe_count", nullable = false)
    private long recipeCount;

    protected RecipeLookupCountEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getTargetDomain() { return targetDomain; }
    public String getTargetId() { return targetId; }
    public String getLookupKind() { return lookupKind; }
    public long getRecipeCount() { return recipeCount; }

    public static class RecipeLookupCountId {
        private String datasetId;
        private String targetDomain;
        private String targetId;
        private String lookupKind;

        public RecipeLookupCountId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecipeLookupCountId other)) return false;
            return datasetId.equals(other.datasetId)
                    && targetDomain.equals(other.targetDomain)
                    && targetId.equals(other.targetId)
                    && lookupKind.equals(other.lookupKind);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + targetDomain.hashCode();
            result = 31 * result + targetId.hashCode();
            result = 31 * result + lookupKind.hashCode();
            return result;
        }
    }
}
