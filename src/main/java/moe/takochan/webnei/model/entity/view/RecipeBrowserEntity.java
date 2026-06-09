package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeBrowserEntity.RecipeBrowserId.class)
@Table(name = "v_recipe_browser")
public class RecipeBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "category_display_name", nullable = false)
    private String categoryDisplayName;

    @Column(name = "source_plugin", nullable = false)
    private String sourcePlugin;

    @Column(name = "source_ref", nullable = false)
    private String sourceRef;

    @Column(nullable = false)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "handler_id", nullable = false)
    private String handlerId;

    @Column(nullable = false)
    private String plugin;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "mod_name", nullable = false)
    private String modName;

    protected RecipeBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryDisplayName() { return categoryDisplayName; }
    public String getSourcePlugin() { return sourcePlugin; }
    public String getSourceRef() { return sourceRef; }
    public String getDescription() { return description; }
    public int getDisplayOrder() { return displayOrder; }
    public String getHandlerId() { return handlerId; }
    public String getPlugin() { return plugin; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }

    public static class RecipeBrowserId {
        private String datasetId;
        private String recipeId;

        public RecipeBrowserId() {}
        public RecipeBrowserId(String datasetId, String recipeId) {
            this.datasetId = datasetId;
            this.recipeId = recipeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecipeBrowserId other)) return false;
            return datasetId.equals(other.datasetId) && recipeId.equals(other.recipeId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + recipeId.hashCode(); }
    }
}
