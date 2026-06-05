package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeLookupBreakdownEntity.LookupBreakdownId.class)
@Table(name = "v_recipe_lookup_breakdown")
public class RecipeLookupBreakdownEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Id
    @Column(name = "lookup_kind", nullable = false)
    private String lookupKind;

    @Id
    @Column(name = "handler_id", nullable = false)
    private String handlerId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "icon_asset_path")
    private String iconAssetPath;

    @Column(name = "icon_image_resource")
    private String iconImageResource;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "recipe_count", nullable = false)
    private long recipeCount;

    protected RecipeLookupBreakdownEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getTargetId() { return targetId; }
    public String getLookupKind() { return lookupKind; }
    public String getHandlerId() { return handlerId; }
    public String getCategoryId() { return categoryId; }
    public String getDisplayName() { return displayName; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconImageResource() { return iconImageResource; }
    public int getDisplayOrder() { return displayOrder; }
    public long getRecipeCount() { return recipeCount; }

    public static class LookupBreakdownId {
        private String datasetId;
        private String targetId;
        private String lookupKind;
        private String handlerId;
        private String categoryId;

        public LookupBreakdownId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LookupBreakdownId other)) return false;
            return datasetId.equals(other.datasetId)
                    && targetId.equals(other.targetId)
                    && lookupKind.equals(other.lookupKind)
                    && handlerId.equals(other.handlerId)
                    && categoryId.equals(other.categoryId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + targetId.hashCode();
            result = 31 * result + lookupKind.hashCode();
            result = 31 * result + handlerId.hashCode();
            result = 31 * result + categoryId.hashCode();
            return result;
        }
    }
}
