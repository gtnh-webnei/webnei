package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemDetailEntity.ItemDetailId.class)
@Table(name = "v_item_detail")
public class ItemDetailEntity {

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

    @Column(name = "unlocalized_name", nullable = false)
    private String unlocalizedName;

    @Column(name = "max_stack_size", nullable = false)
    private int maxStackSize;

    @Column(name = "max_damage", nullable = false)
    private int maxDamage;

    @Column(nullable = false)
    private int damage;

    @Column(name = "nbt_text", nullable = false)
    private String nbtText;

    @Column(name = "chemical_expression")
    private String chemicalExpression;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "tooltip_text", nullable = false)
    private String tooltipText;

    protected ItemDetailEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getItemId() { return itemId; }
    public String getModId() { return modId; }
    public String getModName() { return modName; }
    public String getRegistryName() { return registryName; }
    public String getUnlocalizedName() { return unlocalizedName; }
    public int getMaxStackSize() { return maxStackSize; }
    public int getMaxDamage() { return maxDamage; }
    public int getDamage() { return damage; }
    public String getNbtText() { return nbtText; }
    public String getChemicalExpression() { return chemicalExpression; }
    public String getDisplayName() { return displayName; }
    public String getTooltipText() { return tooltipText; }

    public static class ItemDetailId {
        private String datasetId;
        private String itemVariantId;

        public ItemDetailId() {}
        public ItemDetailId(String datasetId, String itemVariantId) {
            this.datasetId = datasetId;
            this.itemVariantId = itemVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemDetailId other)) return false;
            return datasetId.equals(other.datasetId) && itemVariantId.equals(other.itemVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemVariantId.hashCode(); }
    }
}
