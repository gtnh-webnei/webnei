package moe.takochan.webnei.recipe;

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

    @Column(name = "item_display_name")
    private String itemDisplayName;

    @Column(name = "item_mod_id")
    private String itemModId;

    @Column(name = "item_asset_path")
    private String itemAssetPath;

    @Column(name = "fluid_display_name")
    private String fluidDisplayName;

    @Column(name = "fluid_mod_id")
    private String fluidModId;

    @Column(name = "fluid_asset_path")
    private String fluidAssetPath;

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
    public String getItemDisplayName() { return itemDisplayName; }
    public String getItemModId() { return itemModId; }
    public String getItemAssetPath() { return itemAssetPath; }
    public String getFluidDisplayName() { return fluidDisplayName; }
    public String getFluidModId() { return fluidModId; }
    public String getFluidAssetPath() { return fluidAssetPath; }

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
