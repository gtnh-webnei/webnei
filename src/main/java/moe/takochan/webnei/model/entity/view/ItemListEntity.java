package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemListEntity.ItemListId.class)
@Table(name = "v_item_list")
public class ItemListEntity {

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

    @Column(name = "registry_name", nullable = false)
    private String registryName;

    @Column(nullable = false)
    private int damage;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "asset_path")
    private String assetPath;

    @Column(name = "asset_sha256")
    private String assetSha256;

    @Column(name = "panel_index", nullable = false)
    private int panelIndex;

    protected ItemListEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getItemId() { return itemId; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getRegistryName() { return registryName; }
    public int getDamage() { return damage; }
    public String getDisplayName() { return displayName; }
    public String getAssetPath() { return assetPath; }
    public String getAssetSha256() { return assetSha256; }
    public int getPanelIndex() { return panelIndex; }

    public static class ItemListId {
        private String datasetId;
        private String itemVariantId;

        public ItemListId() {}
        public ItemListId(String datasetId, String itemVariantId) {
            this.datasetId = datasetId;
            this.itemVariantId = itemVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemListId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
