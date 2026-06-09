package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreVeinDimensionEntity.OreVeinDimensionId.class)
@Table(name = "gt_ore_vein_dimension")
public class GtOreVeinDimensionEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "vein_name", nullable = false)
    private String veinName;

    @Id
    @Column(nullable = false)
    private String dimension;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "display_abbr", nullable = false)
    private String displayAbbr;

    protected GtOreVeinDimensionEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getVeinName() { return veinName; }
    public String getDimension() { return dimension; }
    public boolean isEnabled() { return enabled; }
    public String getDisplayAbbr() { return displayAbbr; }

    public static class OreVeinDimensionId {
        private String datasetId;
        private String veinName;
        private String dimension;

        public OreVeinDimensionId() {}
        public OreVeinDimensionId(String datasetId, String veinName, String dimension) {
            this.datasetId = datasetId;
            this.veinName = veinName;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreVeinDimensionId other)) return false;
            return datasetId.equals(other.datasetId)
                    && veinName.equals(other.veinName)
                    && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + veinName.hashCode()) * 31 + dimension.hashCode(); }
    }
}
