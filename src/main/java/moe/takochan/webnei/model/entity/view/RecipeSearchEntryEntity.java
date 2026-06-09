package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeSearchEntryEntity.SearchEntryId.class)
@Table(name = "v_recipe_search_entry")
public class RecipeSearchEntryEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(name = "target_domain", nullable = false)
    private String targetDomain;

    @Id
    @Column(name = "target_id", nullable = false)
    private String targetId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "search_text")
    private String searchText;

    protected RecipeSearchEntryEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getCategoryId() { return categoryId; }
    public String getTargetDomain() { return targetDomain; }
    public String getTargetId() { return targetId; }
    public String getDisplayName() { return displayName; }
    public String getSearchText() { return searchText; }

    public static class SearchEntryId {
        private String datasetId;
        private String recipeId;
        private String targetDomain;
        private String targetId;

        public SearchEntryId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SearchEntryId other)) return false;
            return datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId)
                    && targetDomain.equals(other.targetDomain)
                    && targetId.equals(other.targetId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + targetDomain.hashCode();
            result = 31 * result + targetId.hashCode();
            return result;
        }
    }
}
