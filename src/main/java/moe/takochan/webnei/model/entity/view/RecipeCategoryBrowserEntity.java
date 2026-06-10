package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeCategoryBrowserEntity.CategoryId.class)
@Table(name = "v_recipe_category_browser")
public class RecipeCategoryBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String plugin;

    @Column(name = "handler_id", nullable = false)
    private String handlerId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean shapeless;

    @Column(name = "icon_display_name")
    private String iconDisplayName;

    @Column(name = "icon_asset_id")
    private String iconAssetId;

    @Column(name = "icon_asset_path")
    private String iconAssetPath;

    @Column(name = "icon_info")
    private String iconInfo;

    @Column(name = "item_input_width", nullable = false)
    private int itemInputWidth;

    @Column(name = "item_input_height", nullable = false)
    private int itemInputHeight;

    @Column(name = "fluid_input_width", nullable = false)
    private int fluidInputWidth;

    @Column(name = "fluid_input_height", nullable = false)
    private int fluidInputHeight;

    @Column(name = "item_output_width", nullable = false)
    private int itemOutputWidth;

    @Column(name = "item_output_height", nullable = false)
    private int itemOutputHeight;

    @Column(name = "fluid_output_width", nullable = false)
    private int fluidOutputWidth;

    @Column(name = "fluid_output_height", nullable = false)
    private int fluidOutputHeight;

    @Column(name = "supports_recipe_lookup", nullable = false)
    private boolean supportsRecipeLookup;

    @Column(name = "supports_usage_lookup", nullable = false)
    private boolean supportsUsageLookup;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "canvas_width")
    private Integer canvasWidth;

    @Column(name = "canvas_height")
    private Integer canvasHeight;

    @Column(name = "background_asset_id")
    private String backgroundAssetId;

    @Column(name = "background_asset_path")
    private String backgroundAssetPath;

    @Column(name = "recipe_count", nullable = false)
    private long recipeCount;

    @Column(name = "applicable_item_count", nullable = false)
    private long applicableItemCount;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "mod_name", nullable = false)
    private String modName;

    @Column(name = "handler_class")
    private String handlerClass;

    @Column(name = "handler_canvas_width", nullable = false)
    private int handlerCanvasWidth;

    @Column(name = "handler_canvas_height", nullable = false)
    private int handlerCanvasHeight;

    @Column(name = "handler_y_shift", nullable = false)
    private int handlerYShift;

    @Column(name = "handler_multiple_widgets_allowed", nullable = false)
    private boolean handlerMultipleWidgetsAllowed;

    @Column(name = "icon_image_resource")
    private String iconImageResource;

    @Column(name = "icon_image_x", nullable = false)
    private int iconImageX;

    @Column(name = "icon_image_y", nullable = false)
    private int iconImageY;

    @Column(name = "icon_image_width", nullable = false)
    private int iconImageWidth;

    @Column(name = "icon_image_height", nullable = false)
    private int iconImageHeight;

    @Column(name = "icon_image_texture_width", nullable = false)
    private int iconImageTextureWidth;

    @Column(name = "icon_image_texture_height", nullable = false)
    private int iconImageTextureHeight;

    protected RecipeCategoryBrowserEntity() {}

    // Getters
    public String getDatasetId() { return datasetId; }
    public String getCategoryId() { return categoryId; }
    public String getPlugin() { return plugin; }
    public String getHandlerId() { return handlerId; }
    public String getDisplayName() { return displayName; }
    public boolean isShapeless() { return shapeless; }
    public String getIconDisplayName() { return iconDisplayName; }
    public String getIconAssetId() { return iconAssetId; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconInfo() { return iconInfo; }
    public int getItemInputWidth() { return itemInputWidth; }
    public int getItemInputHeight() { return itemInputHeight; }
    public int getFluidInputWidth() { return fluidInputWidth; }
    public int getFluidInputHeight() { return fluidInputHeight; }
    public int getItemOutputWidth() { return itemOutputWidth; }
    public int getItemOutputHeight() { return itemOutputHeight; }
    public int getFluidOutputWidth() { return fluidOutputWidth; }
    public int getFluidOutputHeight() { return fluidOutputHeight; }
    public boolean isSupportsRecipeLookup() { return supportsRecipeLookup; }
    public boolean isSupportsUsageLookup() { return supportsUsageLookup; }
    public int getDisplayOrder() { return displayOrder; }
    public Integer getCanvasWidth() { return canvasWidth; }
    public Integer getCanvasHeight() { return canvasHeight; }
    public String getBackgroundAssetId() { return backgroundAssetId; }
    public String getBackgroundAssetPath() { return backgroundAssetPath; }
    public long getRecipeCount() { return recipeCount; }
    public long getApplicableItemCount() { return applicableItemCount; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getHandlerClass() { return handlerClass; }
    public int getHandlerCanvasWidth() { return handlerCanvasWidth; }
    public int getHandlerCanvasHeight() { return handlerCanvasHeight; }
    public int getHandlerYShift() { return handlerYShift; }
    public boolean isHandlerMultipleWidgetsAllowed() { return handlerMultipleWidgetsAllowed; }
    public String getIconImageResource() { return iconImageResource; }
    public int getIconImageX() { return iconImageX; }
    public int getIconImageY() { return iconImageY; }
    public int getIconImageWidth() { return iconImageWidth; }
    public int getIconImageHeight() { return iconImageHeight; }
    public int getIconImageTextureWidth() { return iconImageTextureWidth; }
    public int getIconImageTextureHeight() { return iconImageTextureHeight; }

    public static class CategoryId {
        private String datasetId;
        private String categoryId;

        public CategoryId() {}
        public CategoryId(String datasetId, String categoryId) { this.datasetId = datasetId; this.categoryId = categoryId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CategoryId other)) return false;
            return datasetId.equals(other.datasetId) && categoryId.equals(other.categoryId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + categoryId.hashCode(); }
    }
}
