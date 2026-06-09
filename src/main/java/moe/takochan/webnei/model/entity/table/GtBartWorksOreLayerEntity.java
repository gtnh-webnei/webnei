package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtBartWorksOreLayerEntity.BartWorksOreLayerId.class)
@Table(name = "gt_bartworks_ore_layer")
public class GtBartWorksOreLayerEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "entry_id", nullable = false)
    private String entryId;

    @Id
    @Column(nullable = false)
    private String layer;

    @Column(name = "layer_index", nullable = false)
    private int layerIndex;

    @Column(name = "ore_meta", nullable = false)
    private int oreMeta;

    @Column(name = "bartworks_ore", nullable = false)
    private boolean bartworksOre;

    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    protected GtBartWorksOreLayerEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getEntryId() { return entryId; }
    public String getLayer() { return layer; }
    public int getLayerIndex() { return layerIndex; }
    public int getOreMeta() { return oreMeta; }
    public boolean isBartworksOre() { return bartworksOre; }
    public String getItemVariantId() { return itemVariantId; }
    public String getDisplayName() { return displayName; }

    public static class BartWorksOreLayerId {
        private String datasetId;
        private String entryId;
        private String layer;

        public BartWorksOreLayerId() {}
        public BartWorksOreLayerId(String datasetId, String entryId, String layer) {
            this.datasetId = datasetId;
            this.entryId = entryId;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BartWorksOreLayerId other)) return false;
            return datasetId.equals(other.datasetId)
                    && entryId.equals(other.entryId)
                    && layer.equals(other.layer);
        }

        @Override
        public int hashCode() { return (datasetId.hashCode() * 31 + entryId.hashCode()) * 31 + layer.hashCode(); }
    }
}
