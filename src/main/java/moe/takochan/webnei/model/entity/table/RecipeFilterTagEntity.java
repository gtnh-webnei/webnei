package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

/**
 * Generic filterable recipe tag, backing the {@code recipe_filter_tag} table. Dimensions that need
 * SQL filtering/sorting (e.g. GregTech voltage_tier) live here. {@code tagValue} is the display
 * string; {@code sortValue} gives a numeric ordering key.
 */
@Entity
@Immutable
@IdClass(RecipeFilterTagEntity.FilterTagId.class)
@Table(name = "recipe_filter_tag")
public class RecipeFilterTagEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Id
    @Column(name = "tag_key", nullable = false)
    private String tagKey;

    @Column(name = "tag_value", nullable = false)
    private String tagValue;

    @Column(name = "sort_value")
    private Double sortValue;

    protected RecipeFilterTagEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getTagKey() { return tagKey; }
    public String getTagValue() { return tagValue; }
    public Double getSortValue() { return sortValue; }

    public static class FilterTagId {
        private String datasetId;
        private String recipeId;
        private String tagKey;

        public FilterTagId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FilterTagId other)) return false;
            return datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId)
                    && tagKey.equals(other.tagKey);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + tagKey.hashCode();
            return result;
        }
    }
}
