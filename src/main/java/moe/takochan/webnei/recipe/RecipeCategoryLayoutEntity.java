package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeCategoryLayoutEntity.CategoryLayoutId.class)
@Table(name = "v_recipe_category_base")
public class RecipeCategoryLayoutEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "canvas_width")
    private Integer canvasWidth;

    @Column(name = "canvas_height")
    private Integer canvasHeight;

    @Column(name = "background_asset_path")
    private String backgroundAssetPath;

    protected RecipeCategoryLayoutEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getCategoryId() { return categoryId; }
    public Integer getCanvasWidth() { return canvasWidth; }
    public Integer getCanvasHeight() { return canvasHeight; }
    public String getBackgroundAssetPath() { return backgroundAssetPath; }

    public static class CategoryLayoutId {
        private String datasetId;
        private String categoryId;

        public CategoryLayoutId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CategoryLayoutId other)) return false;
            return datasetId.equals(other.datasetId) && categoryId.equals(other.categoryId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + categoryId.hashCode(); }
    }
}
