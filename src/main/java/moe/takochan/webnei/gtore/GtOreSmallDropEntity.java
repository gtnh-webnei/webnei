package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreSmallDropEntity.OreSmallDropId.class)
@Table(name = "gt_ore_small_drop")
public class GtOreSmallDropEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "ore_gen_name", nullable = false)
    private String oreGenName;

    @Id
    @Column(name = "drop_index", nullable = false)
    private int dropIndex;

    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    protected GtOreSmallDropEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getOreGenName() { return oreGenName; }
    public int getDropIndex() { return dropIndex; }
    public String getItemVariantId() { return itemVariantId; }

    public static class OreSmallDropId {
        private String datasetId;
        private String oreGenName;
        private int dropIndex;

        public OreSmallDropId() {}
        public OreSmallDropId(String datasetId, String oreGenName, int dropIndex) {
            this.datasetId = datasetId;
            this.oreGenName = oreGenName;
            this.dropIndex = dropIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreSmallDropId other)) return false;
            return dropIndex == other.dropIndex
                    && datasetId.equals(other.datasetId)
                    && oreGenName.equals(other.oreGenName);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + oreGenName.hashCode()) * 31 + dropIndex; }
    }
}
