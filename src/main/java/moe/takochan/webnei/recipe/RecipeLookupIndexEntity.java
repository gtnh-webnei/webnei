package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeLookupIndexEntity.RecipeLookupIndexId.class)
@Table(name = "recipe_lookup_index")
public class RecipeLookupIndexEntity {

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

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Id
    @Column(nullable = false)
    private String role;

    @Id
    @Column(name = "slot_index", nullable = false)
    private int slotIndex;

    protected RecipeLookupIndexEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getTargetDomain() { return targetDomain; }
    public String getTargetId() { return targetId; }
    public String getLookupKind() { return lookupKind; }
    public String getRecipeId() { return recipeId; }
    public String getRole() { return role; }
    public int getSlotIndex() { return slotIndex; }

    public static class RecipeLookupIndexId {
        private String datasetId;
        private String targetDomain;
        private String targetId;
        private String lookupKind;
        private String recipeId;
        private String role;
        private int slotIndex;

        public RecipeLookupIndexId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecipeLookupIndexId other)) return false;
            return slotIndex == other.slotIndex
                    && datasetId.equals(other.datasetId)
                    && targetDomain.equals(other.targetDomain)
                    && targetId.equals(other.targetId)
                    && lookupKind.equals(other.lookupKind)
                    && recipeId.equals(other.recipeId)
                    && role.equals(other.role);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + targetDomain.hashCode();
            result = 31 * result + targetId.hashCode();
            result = 31 * result + lookupKind.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + role.hashCode();
            result = 31 * result + slotIndex;
            return result;
        }
    }
}
