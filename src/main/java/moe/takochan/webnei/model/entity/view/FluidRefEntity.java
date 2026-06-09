package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidRefEntity.FluidRefId.class)
@Table(name = "v_fluid_ref")
public class FluidRefEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Column(name = "fluid_id", nullable = false)
    private String fluidId;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "mod_name", nullable = false)
    private String modName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean gaseous;

    @Column(nullable = false)
    private int temperature;

    @Column(name = "asset_path")
    private String assetPath;

    protected FluidRefEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getFluidId() { return fluidId; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getDisplayName() { return displayName; }
    public boolean isGaseous() { return gaseous; }
    public int getTemperature() { return temperature; }
    public String getAssetPath() { return assetPath; }

    public static class FluidRefId {
        private String datasetId;
        private String fluidVariantId;

        public FluidRefId() {}
        public FluidRefId(String datasetId, String fluidVariantId) {
            this.datasetId = datasetId;
            this.fluidVariantId = fluidVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidRefId other)) return false;
            return datasetId.equals(other.datasetId) && fluidVariantId.equals(other.fluidVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + fluidVariantId.hashCode(); }
    }
}
