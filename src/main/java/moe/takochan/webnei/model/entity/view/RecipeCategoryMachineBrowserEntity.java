package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeCategoryMachineBrowserEntity.MachineBrowserId.class)
@Table(name = "v_recipe_category_machine_browser")
public class RecipeCategoryMachineBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Id
    @Column(nullable = false)
    private String role;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "asset_path")
    private String assetPath;

    @Column(name = "opens_category", nullable = false)
    private boolean opensCategory;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "source_ref", nullable = false)
    private String sourceRef;

    protected RecipeCategoryMachineBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getCategoryId() { return categoryId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public String getAssetPath() { return assetPath; }
    public boolean isOpensCategory() { return opensCategory; }
    public int getDisplayOrder() { return displayOrder; }
    public String getSourceRef() { return sourceRef; }

    public static class MachineBrowserId {
        private String datasetId;
        private String categoryId;
        private String itemVariantId;
        private String role;

        public MachineBrowserId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MachineBrowserId other)) return false;
            return datasetId.equals(other.datasetId)
                    && categoryId.equals(other.categoryId)
                    && itemVariantId.equals(other.itemVariantId)
                    && role.equals(other.role);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + categoryId.hashCode();
            result = 31 * result + itemVariantId.hashCode();
            result = 31 * result + role.hashCode();
            return result;
        }
    }
}
