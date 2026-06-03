package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(NeiPanelEntryEntity.NeiPanelEntryId.class)
@Table(name = "nei_item_panel_entry")
public class NeiPanelEntryEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Column(name = "panel_index", nullable = false)
    private int panelIndex;

    @Column(name = "collapsible_collection_id", nullable = false)
    private String collapsibleCollectionId;

    @Column(name = "visible_when_collapsed", nullable = false)
    private boolean visibleWhenCollapsed;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", insertable = false, updatable = false),
            @JoinColumn(name = "item_variant_id", referencedColumnName = "item_variant_id", insertable = false, updatable = false)
    })
    private ItemVariantBrowserEntity itemVariant;

    protected NeiPanelEntryEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public int getPanelIndex() { return panelIndex; }
    public String getCollapsibleCollectionId() { return collapsibleCollectionId; }
    public boolean isVisibleWhenCollapsed() { return visibleWhenCollapsed; }
    public ItemVariantBrowserEntity getItemVariant() { return itemVariant; }

    public static class NeiPanelEntryId {
        private String datasetId;
        private String itemVariantId;

        public NeiPanelEntryId() {}
        public NeiPanelEntryId(String datasetId, String itemVariantId) { this.datasetId = datasetId; this.itemVariantId = itemVariantId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NeiPanelEntryId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
