package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemSearchDocumentEntity.ItemSearchDocumentId.class)
@Table(name = "item_search_document")
public class ItemSearchDocumentEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "panel_index", nullable = false)
    private int panelIndex;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected ItemSearchDocumentEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getItemId() { return itemId; }
    public String getModId() { return modId; }
    public int getPanelIndex() { return panelIndex; }
    public String getSearchText() { return searchText; }

    public static class ItemSearchDocumentId {
        private String datasetId;
        private String itemVariantId;

        public ItemSearchDocumentId() {}
        public ItemSearchDocumentId(String datasetId, String itemVariantId) {
            this.datasetId = datasetId;
            this.itemVariantId = itemVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemSearchDocumentId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
