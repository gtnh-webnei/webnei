package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreVeinLayerEntity.OreVeinLayerId.class)
@Table(name = "gt_ore_vein_layer")
public class GtOreVeinLayerEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "vein_name", nullable = false)
    private String veinName;

    @Id
    @Column(nullable = false)
    private String layer;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "ore_meta", nullable = false)
    private int oreMeta;

    @Column(name = "block_item_variant_id", nullable = false)
    private String blockItemVariantId;

    protected GtOreVeinLayerEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getVeinName() { return veinName; }
    public String getLayer() { return layer; }
    public String getMaterialName() { return materialName; }
    public int getOreMeta() { return oreMeta; }
    public String getBlockItemVariantId() { return blockItemVariantId; }

    public static class OreVeinLayerId {
        private String datasetId;
        private String veinName;
        private String layer;

        public OreVeinLayerId() {}
        public OreVeinLayerId(String datasetId, String veinName, String layer) {
            this.datasetId = datasetId;
            this.veinName = veinName;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreVeinLayerId other)) return false;
            return datasetId.equals(other.datasetId)
                    && veinName.equals(other.veinName)
                    && layer.equals(other.layer);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + veinName.hashCode()) * 31 + layer.hashCode(); }
    }
}
