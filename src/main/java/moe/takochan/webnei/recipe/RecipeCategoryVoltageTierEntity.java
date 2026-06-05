package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeCategoryVoltageTierEntity.VoltageTierId.class)
@Table(name = "v_recipe_category_voltage_tier")
public class RecipeCategoryVoltageTierEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(name = "voltage_tier", nullable = false)
    private String voltageTier;

    @Column(name = "recipe_count", nullable = false)
    private long recipeCount;

    @Column(name = "min_voltage")
    private Integer minVoltage;

    protected RecipeCategoryVoltageTierEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getCategoryId() { return categoryId; }
    public String getVoltageTier() { return voltageTier; }
    public long getRecipeCount() { return recipeCount; }
    public Integer getMinVoltage() { return minVoltage; }

    public static class VoltageTierId {
        private String datasetId;
        private String categoryId;
        private String voltageTier;

        public VoltageTierId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VoltageTierId other)) return false;
            return datasetId.equals(other.datasetId)
                    && categoryId.equals(other.categoryId)
                    && voltageTier.equals(other.voltageTier);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + categoryId.hashCode();
            result = 31 * result + voltageTier.hashCode();
            return result;
        }
    }
}
