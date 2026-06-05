package moe.takochan.webnei.fluid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidVariantBrowserEntity.FluidVariantId.class)
@Table(name = "v_fluid_variant_browser")
public class FluidVariantBrowserEntity {

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

    @Column(name = "registry_name", nullable = false)
    private String registryName;

    @Column(name = "unlocalized_name", nullable = false)
    private String unlocalizedName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "runtime_fluid_id", nullable = false)
    private int runtimeFluidId;

    @Column(nullable = false)
    private int luminosity;

    @Column(nullable = false)
    private int density;

    @Column(nullable = false)
    private int temperature;

    @Column(nullable = false)
    private int viscosity;

    @Column(nullable = false)
    private boolean gaseous;

    @Column(name = "nbt_hash", nullable = false)
    private String nbtHash;

    @Column(name = "nbt_text", nullable = false)
    private String nbtText;

    @Column(name = "chemical_expression")
    private String chemicalExpression;

    @Column(name = "fluid_search_text")
    private String fluidSearchText;

    @Column(name = "variant_search_text")
    private String variantSearchText;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "asset_path")
    private String assetPath;

    protected FluidVariantBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getFluidId() { return fluidId; }
    public String getModId() { return modId; }
    public String getRegistryName() { return registryName; }
    public String getUnlocalizedName() { return unlocalizedName; }
    public String getDisplayName() { return displayName; }
    public int getRuntimeFluidId() { return runtimeFluidId; }
    public int getLuminosity() { return luminosity; }
    public int getDensity() { return density; }
    public int getTemperature() { return temperature; }
    public int getViscosity() { return viscosity; }
    public boolean isGaseous() { return gaseous; }
    public String getNbtHash() { return nbtHash; }
    public String getNbtText() { return nbtText; }
    public String getChemicalExpression() { return chemicalExpression; }
    public String getFluidSearchText() { return fluidSearchText; }
    public String getVariantSearchText() { return variantSearchText; }
    public String getAssetId() { return assetId; }
    public String getAssetPath() { return assetPath; }

    public static class FluidVariantId {
        private String datasetId;
        private String fluidVariantId;

        public FluidVariantId() {}
        public FluidVariantId(String datasetId, String fluidVariantId) { this.datasetId = datasetId; this.fluidVariantId = fluidVariantId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidVariantId other)) return false;
            return datasetId.equals(other.datasetId) && fluidVariantId.equals(other.fluidVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + fluidVariantId.hashCode(); }
    }
}
