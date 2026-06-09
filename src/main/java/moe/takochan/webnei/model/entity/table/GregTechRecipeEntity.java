package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GregTechRecipeEntity.GregTechRecipeId.class)
@Table(name = "gregtech_recipe")
public class GregTechRecipeEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "recipe_kind", nullable = false)
    private String recipeKind;

    @Column(name = "visible_in_nei", nullable = false)
    private boolean visibleInNei;

    @Column(name = "voltage_tier")
    private String voltageTier;

    @Column
    private Integer voltage;

    @Column
    private Integer amperage;

    @Column(name = "duration_ticks", nullable = false)
    private int durationTicks;

    @Column(name = "special_value")
    private Integer specialValue;

    protected GregTechRecipeEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getRecipeKind() { return recipeKind; }
    public boolean isVisibleInNei() { return visibleInNei; }
    public String getVoltageTier() { return voltageTier; }
    public Integer getVoltage() { return voltage; }
    public Integer getAmperage() { return amperage; }
    public int getDurationTicks() { return durationTicks; }
    public Integer getSpecialValue() { return specialValue; }

    public static class GregTechRecipeId {
        private String datasetId;
        private String recipeId;

        public GregTechRecipeId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GregTechRecipeId other)) return false;
            return datasetId.equals(other.datasetId) && recipeId.equals(other.recipeId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + recipeId.hashCode(); }
    }
}
