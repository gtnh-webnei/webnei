package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreSmallVariantEntity.OreSmallVariantId.class)
@Table(name = "gt_ore_small_variant")
public class GtOreSmallVariantEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "ore_gen_name", nullable = false)
    private String oreGenName;

    @Id
    @Column(name = "variant_index", nullable = false)
    private int variantIndex;

    @Column(name = "small_ore_item_variant_id", nullable = false)
    private String smallOreItemVariantId;

    @Column(name = "dust_item_variant_id", nullable = false)
    private String dustItemVariantId;

    protected GtOreSmallVariantEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getOreGenName() { return oreGenName; }
    public int getVariantIndex() { return variantIndex; }
    public String getSmallOreItemVariantId() { return smallOreItemVariantId; }
    public String getDustItemVariantId() { return dustItemVariantId; }

    public static class OreSmallVariantId {
        private String datasetId;
        private String oreGenName;
        private int variantIndex;

        public OreSmallVariantId() {}
        public OreSmallVariantId(String datasetId, String oreGenName, int variantIndex) {
            this.datasetId = datasetId;
            this.oreGenName = oreGenName;
            this.variantIndex = variantIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreSmallVariantId other)) return false;
            return variantIndex == other.variantIndex
                    && datasetId.equals(other.datasetId)
                    && oreGenName.equals(other.oreGenName);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + oreGenName.hashCode()) * 31 + variantIndex; }
    }
}
