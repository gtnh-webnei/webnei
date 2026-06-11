package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeSlotMetadataEntity.SlotMetadataId.class)
@Table(name = "recipe_slot_metadata")
public class RecipeSlotMetadataEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Id
    @Column(nullable = false)
    private String role;

    @Id
    @Column(name = "slot_index", nullable = false)
    private int slotIndex;

    @Id
    @Column(name = "metadata_key", nullable = false)
    private String metadataKey;

    @Column(name = "value_type", nullable = false)
    private String valueType;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "value_json")
    private String valueJson;

    protected RecipeSlotMetadataEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getRole() { return role; }
    public int getSlotIndex() { return slotIndex; }
    public String getMetadataKey() { return metadataKey; }
    public String getValueType() { return valueType; }
    public String getValueText() { return valueText; }
    public String getValueJson() { return valueJson; }

    public static class SlotMetadataId {
        private String datasetId;
        private String recipeId;
        private String role;
        private int slotIndex;
        private String metadataKey;

        public SlotMetadataId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SlotMetadataId other)) return false;
            return slotIndex == other.slotIndex
                    && datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId)
                    && role.equals(other.role)
                    && metadataKey.equals(other.metadataKey);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + role.hashCode();
            result = 31 * result + slotIndex;
            result = 31 * result + metadataKey.hashCode();
            return result;
        }
    }
}
