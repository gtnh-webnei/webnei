package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtUndergroundFluidBrowserEntity.UndergroundFluidBrowserId.class)
@Table(name = "v_gt_underground_fluid_browser")
public class GtUndergroundFluidBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_id", nullable = false)
    private String fluidId;

    @Id
    @Column(nullable = false)
    private String dimension;

    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Column(nullable = false)
    private int chance;

    @Column(name = "min_amount", nullable = false)
    private int minAmount;

    @Column(name = "max_amount", nullable = false)
    private int maxAmount;

    @Column(name = "fluid_display_name")
    private String fluidDisplayName;

    @Column(name = "fluid_asset_path")
    private String fluidAssetPath;

    @Column(name = "dimension_full_name")
    private String dimensionFullName;

    @Column(name = "dimension_display_name")
    private String dimensionDisplayName;

    @Column(name = "dimension_display_abbr")
    private String dimensionDisplayAbbr;

    @Column(name = "dimension_icon_item_variant_id")
    private String dimensionIconItemVariantId;

    @Column(name = "dimension_sort_order")
    private Integer dimensionSortOrder;

    protected GtUndergroundFluidBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidId() { return fluidId; }
    public String getDimension() { return dimension; }
    public String getFluidVariantId() { return fluidVariantId; }
    public int getChance() { return chance; }
    public int getMinAmount() { return minAmount; }
    public int getMaxAmount() { return maxAmount; }
    public String getFluidDisplayName() { return fluidDisplayName; }
    public String getFluidAssetPath() { return fluidAssetPath; }
    public String getDimensionFullName() { return dimensionFullName; }
    public String getDimensionDisplayName() { return dimensionDisplayName; }
    public String getDimensionDisplayAbbr() { return dimensionDisplayAbbr; }
    public String getDimensionIconItemVariantId() { return dimensionIconItemVariantId; }
    public Integer getDimensionSortOrder() { return dimensionSortOrder; }

    public static class UndergroundFluidBrowserId {
        private String datasetId;
        private String fluidId;
        private String dimension;

        public UndergroundFluidBrowserId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UndergroundFluidBrowserId other)) return false;
            return datasetId.equals(other.datasetId)
                    && fluidId.equals(other.fluidId)
                    && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + fluidId.hashCode()) * 31 + dimension.hashCode(); }
    }
}
