package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtUndergroundFluidEntity.UndergroundFluidId.class)
@Table(name = "gt_underground_fluid")
public class GtUndergroundFluidEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_id", nullable = false)
    private String fluidId;

    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Id
    @Column(nullable = false)
    private String dimension;

    @Column(nullable = false)
    private int chance;

    @Column(name = "min_amount", nullable = false)
    private int minAmount;

    @Column(name = "max_amount", nullable = false)
    private int maxAmount;

    protected GtUndergroundFluidEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidId() { return fluidId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getDimension() { return dimension; }
    public int getChance() { return chance; }
    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }

    public static class UndergroundFluidId {
        private String datasetId;
        private String fluidId;
        private String dimension;

        public UndergroundFluidId() {}
        public UndergroundFluidId(String datasetId, String fluidId, String dimension) {
            this.datasetId = datasetId;
            this.fluidId = fluidId;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UndergroundFluidId other)) return false;
            return datasetId.equals(other.datasetId)
                    && fluidId.equals(other.fluidId)
                    && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + fluidId.hashCode()) * 31 + dimension.hashCode(); }
    }
}
