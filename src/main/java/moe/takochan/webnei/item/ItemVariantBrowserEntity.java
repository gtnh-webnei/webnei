package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemVariantBrowserEntity.ItemVariantId.class)
@Table(name = "v_item_variant_browser")
public class ItemVariantBrowserEntity {

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

    @Column(name = "registry_name", nullable = false)
    private String registryName;

    @Column(name = "unlocalized_name", nullable = false)
    private String unlocalizedName;

    @Column(name = "max_stack_size", nullable = false)
    private int maxStackSize;

    @Column(name = "max_damage", nullable = false)
    private int maxDamage;

    @Column(name = "runtime_item_id", nullable = false)
    private int runtimeItemId;

    @Column(nullable = false)
    private int damage;

    @Column(name = "nbt_hash", nullable = false)
    private String nbtHash;

    @Column(name = "nbt_text", nullable = false)
    private String nbtText;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "tooltip_text", nullable = false)
    private String tooltipText;

    @Column(name = "chemical_expression")
    private String chemicalExpression;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "asset_path")
    private String assetPath;

    @Column(name = "asset_sha256")
    private String assetSha256;

    @Column(name = "asset_width")
    private Integer assetWidth;

    @Column(name = "asset_height")
    private Integer assetHeight;

    protected ItemVariantBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getItemId() { return itemId; }
    public String getModId() { return modId; }
    public String getRegistryName() { return registryName; }
    public String getUnlocalizedName() { return unlocalizedName; }
    public int getMaxStackSize() { return maxStackSize; }
    public int getMaxDamage() { return maxDamage; }
    public int getRuntimeItemId() { return runtimeItemId; }
    public int getDamage() { return damage; }
    public String getNbtHash() { return nbtHash; }
    public String getNbtText() { return nbtText; }
    public String getDisplayName() { return displayName; }
    public String getTooltipText() { return tooltipText; }
    public String getChemicalExpression() { return chemicalExpression; }
    public String getAssetId() { return assetId; }
    public String getAssetPath() { return assetPath; }
    public String getAssetSha256() { return assetSha256; }
    public Integer getAssetWidth() { return assetWidth; }
    public Integer getAssetHeight() { return assetHeight; }

    public static class ItemVariantId {
        private String datasetId;
        private String itemVariantId;

        public ItemVariantId() {}
        public ItemVariantId(String datasetId, String itemVariantId) { this.datasetId = datasetId; this.itemVariantId = itemVariantId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemVariantId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
