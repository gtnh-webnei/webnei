package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreVeinLayerVariantEntity.OreVeinLayerVariantId.class)
@Table(name = "gt_ore_vein_layer_variant")
public class GtOreVeinLayerVariantEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "vein_name", nullable = false)
    private String veinName;

    @Id
    @Column(nullable = false)
    private String layer;

    @Id
    @Column(name = "variant_index", nullable = false)
    private int variantIndex;

    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    protected GtOreVeinLayerVariantEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getVeinName() { return veinName; }
    public String getLayer() { return layer; }
    public int getVariantIndex() { return variantIndex; }
    public String getItemVariantId() { return itemVariantId; }

    public static class OreVeinLayerVariantId {
        private String datasetId;
        private String veinName;
        private String layer;
        private int variantIndex;

        public OreVeinLayerVariantId() {}
        public OreVeinLayerVariantId(String datasetId, String veinName, String layer, int variantIndex) {
            this.datasetId = datasetId;
            this.veinName = veinName;
            this.layer = layer;
            this.variantIndex = variantIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreVeinLayerVariantId other)) return false;
            return variantIndex == other.variantIndex
                    && datasetId.equals(other.datasetId)
                    && veinName.equals(other.veinName)
                    && layer.equals(other.layer);
        }

        @Override
        public int hashCode() {
            return ((datasetId.hashCode() * 31 + veinName.hashCode()) * 31 + layer.hashCode()) * 31 + variantIndex;
        }
    }
}
