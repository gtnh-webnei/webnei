package moe.takochan.webnei.mob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(MobDropBrowserEntity.MobDropId.class)
@Table(name = "v_mob_drop_browser")
public class MobDropBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mob_variant_id", nullable = false)
    private String mobVariantId;

    @Id
    @Column(name = "list_index", nullable = false)
    private int listIndex;

    @Column(name = "drop_type", nullable = false)
    private String dropType;

    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Column(name = "stack_size", nullable = false)
    private int stackSize;

    @Column(nullable = false)
    private double probability;

    @Column(nullable = false)
    private boolean lootable;

    @Column(name = "player_only", nullable = false)
    private boolean playerOnly;

    protected MobDropBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getMobVariantId() { return mobVariantId; }
    public int getListIndex() { return listIndex; }
    public String getDropType() { return dropType; }
    public String getItemVariantId() { return itemVariantId; }
    public int getStackSize() { return stackSize; }
    public double getProbability() { return probability; }
    public boolean isLootable() { return lootable; }
    public boolean isPlayerOnly() { return playerOnly; }

    public static class MobDropId {
        private String datasetId;
        private String mobVariantId;
        private int listIndex;

        public MobDropId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MobDropId other)) return false;
            return listIndex == other.listIndex
                    && datasetId.equals(other.datasetId)
                    && mobVariantId.equals(other.mobVariantId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + mobVariantId.hashCode();
            result = 31 * result + listIndex;
            return result;
        }
    }
}
