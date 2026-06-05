package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeLookupBrowserEntity.LookupBrowserId.class)
@Table(name = "v_recipe_lookup_recipe_browser")
public class RecipeLookupBrowserEntity {

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

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "handler_id", nullable = false)
    private String handlerId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "recipe_display_order", nullable = false)
    private int recipeDisplayOrder;

    protected RecipeLookupBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getTargetDomain() { return targetDomain; }
    public String getTargetId() { return targetId; }
    public String getLookupKind() { return lookupKind; }
    public String getRecipeId() { return recipeId; }
    public String getCategoryId() { return categoryId; }
    public String getHandlerId() { return handlerId; }
    public int getDisplayOrder() { return displayOrder; }
    public int getRecipeDisplayOrder() { return recipeDisplayOrder; }

    public static class LookupBrowserId {
        private String datasetId;
        private String targetDomain;
        private String targetId;
        private String lookupKind;
        private String recipeId;

        public LookupBrowserId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LookupBrowserId other)) return false;
            return datasetId.equals(other.datasetId)
                    && targetDomain.equals(other.targetDomain)
                    && targetId.equals(other.targetId)
                    && lookupKind.equals(other.lookupKind)
                    && recipeId.equals(other.recipeId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + targetDomain.hashCode();
            result = 31 * result + targetId.hashCode();
            result = 31 * result + lookupKind.hashCode();
            result = 31 * result + recipeId.hashCode();
            return result;
        }
    }
}
