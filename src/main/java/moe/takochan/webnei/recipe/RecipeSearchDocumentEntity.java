package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeSearchDocumentEntity.RecipeSearchDocumentId.class)
@Table(name = "recipe_search_document")
public class RecipeSearchDocumentEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected RecipeSearchDocumentEntity() {}

    public static class RecipeSearchDocumentId {
        private String datasetId;
        private String recipeId;

        public RecipeSearchDocumentId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecipeSearchDocumentId other)) return false;
            return datasetId.equals(other.datasetId) && recipeId.equals(other.recipeId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + recipeId.hashCode(); }
    }
}
