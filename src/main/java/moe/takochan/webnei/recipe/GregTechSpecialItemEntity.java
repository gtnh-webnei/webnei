package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GregTechSpecialItemEntity.SpecialItemId.class)
@Table(name = "gregtech_recipe_special_item")
public class GregTechSpecialItemEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Id
    @Column(name = "list_index", nullable = false)
    private int listIndex;

    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    protected GregTechSpecialItemEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public int getListIndex() { return listIndex; }
    public String getItemVariantId() { return itemVariantId; }

    public static class SpecialItemId {
        private String datasetId;
        private String recipeId;
        private int listIndex;

        public SpecialItemId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SpecialItemId other)) return false;
            return listIndex == other.listIndex
                    && datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + listIndex;
            return result;
        }
    }
}
