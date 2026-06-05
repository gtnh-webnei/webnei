package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreSmallDimensionEntity.OreSmallDimensionId.class)
@Table(name = "gt_ore_small_dimension")
public class GtOreSmallDimensionEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "ore_gen_name", nullable = false)
    private String oreGenName;

    @Id
    @Column(nullable = false)
    private String dimension;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "display_abbr", nullable = false)
    private String displayAbbr;

    protected GtOreSmallDimensionEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getOreGenName() { return oreGenName; }
    public String getDimension() { return dimension; }
    public boolean isEnabled() { return enabled; }
    public String getDisplayAbbr() { return displayAbbr; }

    public static class OreSmallDimensionId {
        private String datasetId;
        private String oreGenName;
        private String dimension;

        public OreSmallDimensionId() {}
        public OreSmallDimensionId(String datasetId, String oreGenName, String dimension) {
            this.datasetId = datasetId;
            this.oreGenName = oreGenName;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreSmallDimensionId other)) return false;
            return datasetId.equals(other.datasetId)
                    && oreGenName.equals(other.oreGenName)
                    && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + oreGenName.hashCode()) * 31 + dimension.hashCode(); }
    }
}
