package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtBartWorksOreEntity.BartWorksOreId.class)
@Table(name = "gt_bartworks_ore")
public class GtBartWorksOreEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "entry_id", nullable = false)
    private String entryId;

    @Column(name = "entry_type", nullable = false)
    private String entryType;

    @Column(name = "worldgen_name", nullable = false)
    private String worldgenName;

    @Column(nullable = false)
    private String dimension;

    @Column(name = "dimension_display_name", nullable = false)
    private String dimensionDisplayName;

    @Column(name = "result_item_variant_id", nullable = false)
    private String resultItemVariantId;

    @Column(name = "result_display_name", nullable = false)
    private String resultDisplayName;

    @Column(name = "height_min", nullable = false)
    private int heightMin;

    @Column(name = "height_max", nullable = false)
    private int heightMax;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private int density;

    @Column(nullable = false)
    private int size;

    @Column(name = "amount_per_chunk", nullable = false)
    private int amountPerChunk;

    protected GtBartWorksOreEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getEntryId() { return entryId; }
    public String getEntryType() { return entryType; }
    public String getWorldgenName() { return worldgenName; }
    public String getDimension() { return dimension; }
    public String getDimensionDisplayName() { return dimensionDisplayName; }
    public String getResultItemVariantId() { return resultItemVariantId; }
    public String getResultDisplayName() { return resultDisplayName; }
    public int getHeightMin() { return heightMin; }
    public int getHeightMax() { return heightMax; }
    public int getWeight() { return weight; }
    public int getDensity() { return density; }
    public int getSize() { return size; }
    public int getAmountPerChunk() { return amountPerChunk; }

    public static class BartWorksOreId {
        private String datasetId;
        private String entryId;

        public BartWorksOreId() {}
        public BartWorksOreId(String datasetId, String entryId) {
            this.datasetId = datasetId;
            this.entryId = entryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BartWorksOreId other)) return false;
            return datasetId.equals(other.datasetId) && entryId.equals(other.entryId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + entryId.hashCode(); }
    }
}
