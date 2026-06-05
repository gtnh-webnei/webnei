package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreSmallEntity.OreSmallId.class)
@Table(name = "gt_ore_small")
public class GtOreSmallEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "ore_gen_name", nullable = false)
    private String oreGenName;

    @Column(name = "ore_meta", nullable = false)
    private int oreMeta;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(name = "amount_per_chunk", nullable = false)
    private int amountPerChunk;

    @Column(name = "height_min", nullable = false)
    private int heightMin;

    @Column(name = "height_max", nullable = false)
    private int heightMax;

    @Column(name = "small_ore_item_variant_id", nullable = false)
    private String smallOreItemVariantId;

    @Column(name = "dust_item_variant_id", nullable = false)
    private String dustItemVariantId;

    protected GtOreSmallEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getOreGenName() { return oreGenName; }
    public int getOreMeta() { return oreMeta; }
    public String getMaterialName() { return materialName; }
    public int getAmountPerChunk() { return amountPerChunk; }
    public int getHeightMin() { return heightMin; }
    public int getHeightMax() { return heightMax; }
    public String getSmallOreItemVariantId() { return smallOreItemVariantId; }
    public String getDustItemVariantId() { return dustItemVariantId; }

    public static class OreSmallId {
        private String datasetId;
        private String oreGenName;

        public OreSmallId() {}
        public OreSmallId(String datasetId, String oreGenName) {
            this.datasetId = datasetId;
            this.oreGenName = oreGenName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreSmallId other)) return false;
            return datasetId.equals(other.datasetId) && oreGenName.equals(other.oreGenName);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + oreGenName.hashCode(); }
    }
}
