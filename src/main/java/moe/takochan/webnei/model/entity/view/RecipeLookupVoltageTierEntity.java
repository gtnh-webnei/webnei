package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeLookupVoltageTierEntity.LookupVoltageTierId.class)
@Table(name = "v_recipe_lookup_voltage_tier")
public class RecipeLookupVoltageTierEntity {

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
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(name = "voltage_tier", nullable = false)
    private String voltageTier;

    @Column(name = "recipe_count", nullable = false)
    private long recipeCount;

    @Column(name = "min_voltage")
    private Integer minVoltage;

    protected RecipeLookupVoltageTierEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getTargetId() { return targetId; }
    public String getLookupKind() { return lookupKind; }
    public String getCategoryId() { return categoryId; }
    public String getVoltageTier() { return voltageTier; }
    public long getRecipeCount() { return recipeCount; }
    public Integer getMinVoltage() { return minVoltage; }

    public static class LookupVoltageTierId {
        private String datasetId;
        private String targetId;
        private String lookupKind;
        private String categoryId;
        private String voltageTier;

        public LookupVoltageTierId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LookupVoltageTierId other)) return false;
            return datasetId.equals(other.datasetId)
                    && targetId.equals(other.targetId)
                    && lookupKind.equals(other.lookupKind)
                    && categoryId.equals(other.categoryId)
                    && voltageTier.equals(other.voltageTier);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + targetId.hashCode();
            result = 31 * result + lookupKind.hashCode();
            result = 31 * result + categoryId.hashCode();
            result = 31 * result + voltageTier.hashCode();
            return result;
        }
    }
}
