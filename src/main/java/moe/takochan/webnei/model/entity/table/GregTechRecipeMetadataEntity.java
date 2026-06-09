package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GregTechRecipeMetadataEntity.MetadataId.class)
@Table(name = "gregtech_recipe_metadata")
public class GregTechRecipeMetadataEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Id
    @Column(name = "metadata_key", nullable = false)
    private String metadataKey;

    @Column(name = "value_type", nullable = false)
    private String valueType;

    @Column(name = "value_text")
    private String valueText;

    @Column(name = "value_json")
    private String valueJson;

    protected GregTechRecipeMetadataEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getRecipeId() { return recipeId; }
    public String getMetadataKey() { return metadataKey; }
    public String getValueType() { return valueType; }
    public String getValueText() { return valueText; }
    public String getValueJson() { return valueJson; }

    public static class MetadataId {
        private String datasetId;
        private String recipeId;
        private String metadataKey;

        public MetadataId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MetadataId other)) return false;
            return datasetId.equals(other.datasetId)
                    && recipeId.equals(other.recipeId)
                    && metadataKey.equals(other.metadataKey);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + recipeId.hashCode();
            result = 31 * result + metadataKey.hashCode();
            return result;
        }
    }
}
