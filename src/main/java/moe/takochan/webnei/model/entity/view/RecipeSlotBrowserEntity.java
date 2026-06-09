package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeSlotBrowserEntity.SlotBrowserId.class)
@Table(name = "v_recipe_slot_browser")
public class RecipeSlotBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(nullable = false)
    private String role;

    @Id
    @Column(name = "slot_index", nullable = false)
    private int slotIndex;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private double probability;

    @Column(name = "item_variant_id")
    private String itemVariantId;

    @Column(name = "fluid_variant_id")
    private String fluidVariantId;

    protected RecipeSlotBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getCategoryId() { return categoryId; }
    public String getRole() { return role; }
    public int getSlotIndex() { return slotIndex; }
    public String getGroupId() { return groupId; }
    public int getAmount() { return amount; }
    public double getProbability() { return probability; }
    public String getItemVariantId() { return itemVariantId; }
    public String getFluidVariantId() { return fluidVariantId; }

    public static class SlotBrowserId {
        private String datasetId;
        private String recipeId;
        private String role;
        private int slotIndex;

        public SlotBrowserId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SlotBrowserId other)) return false;
            return slotIndex == other.slotIndex
                    && datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId)
                    && role.equals(other.role);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + role.hashCode();
            result = 31 * result + slotIndex;
            return result;
        }
    }
}
