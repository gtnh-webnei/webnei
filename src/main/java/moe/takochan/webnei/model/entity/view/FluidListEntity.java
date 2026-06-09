package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidListEntity.FluidListId.class)
@Table(name = "v_fluid_list")
public class FluidListEntity {

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

    @Column(name = "registry_name", nullable = false)
    private String registryName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private boolean gaseous;

    @Column(nullable = false)
    private int density;

    @Column(nullable = false)
    private int temperature;

    @Column(nullable = false)
    private int viscosity;

    @Column(nullable = false)
    private int luminosity;

    @Column(name = "asset_path")
    private String assetPath;

    protected FluidListEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getFluidId() { return fluidId; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getRegistryName() { return registryName; }
    public String getDisplayName() { return displayName; }
    public boolean isGaseous() { return gaseous; }
    public int getDensity() { return density; }
    public int getTemperature() { return temperature; }
    public int getViscosity() { return viscosity; }
    public int getLuminosity() { return luminosity; }
    public String getAssetPath() { return assetPath; }

    public static class FluidListId {
        private String datasetId;
        private String fluidVariantId;

        public FluidListId() {}
        public FluidListId(String datasetId, String fluidVariantId) {
            this.datasetId = datasetId;
            this.fluidVariantId = fluidVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidListId other)) return false;
            return datasetId.equals(other.datasetId) && fluidVariantId.equals(other.fluidVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + fluidVariantId.hashCode(); }
    }
}
