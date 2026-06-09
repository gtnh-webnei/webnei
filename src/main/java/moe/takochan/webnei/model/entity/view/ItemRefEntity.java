package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemRefEntity.ItemRefId.class)
@Table(name = "v_item_ref")
public class ItemRefEntity {

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

    @Column(name = "mod_name", nullable = false)
    private String modName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "tooltip_text", nullable = false)
    private String tooltipText;

    @Column(name = "asset_path")
    private String assetPath;

    @Column(name = "asset_sha256")
    private String assetSha256;

    protected ItemRefEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getItemId() { return itemId; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getDisplayName() { return displayName; }
    public String getTooltipText() { return tooltipText; }
    public String getAssetPath() { return assetPath; }
    public String getAssetSha256() { return assetSha256; }

    public static class ItemRefId {
        private String datasetId;
        private String itemVariantId;

        public ItemRefId() {}
        public ItemRefId(String datasetId, String itemVariantId) {
            this.datasetId = datasetId;
            this.itemVariantId = itemVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemRefId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
