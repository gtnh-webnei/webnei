package moe.takochan.webnei.mob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(MobVariantBrowserEntity.MobVariantId.class)
@Table(name = "v_mob_variant_browser")
public class MobVariantBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mob_variant_id", nullable = false)
    private String mobVariantId;

    @Column(name = "mob_id", nullable = false)
    private String mobId;

    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(name = "entity_name", nullable = false)
    private String entityName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private double width;

    @Column(nullable = false)
    private double height;

    @Column(name = "max_health", nullable = false)
    private double maxHealth;

    @Column(nullable = false)
    private int armor;

    @Column(name = "immune_to_fire", nullable = false)
    private boolean immuneToFire;

    @Column(nullable = false)
    private boolean leashable;

    @Column(name = "nbt_hash", nullable = false)
    private String nbtHash;

    @Column(name = "nbt_text", nullable = false)
    private String nbtText;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "asset_path")
    private String assetPath;

    protected MobVariantBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getMobVariantId() { return mobVariantId; }
    public String getMobId() { return mobId; }
    public String getModId() { return modId; }
    public String getEntityName() { return entityName; }
    public String getDisplayName() { return displayName; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getMaxHealth() { return maxHealth; }
    public int getArmor() { return armor; }
    public boolean isImmuneToFire() { return immuneToFire; }
    public boolean isLeashable() { return leashable; }
    public String getNbtHash() { return nbtHash; }
    public String getNbtText() { return nbtText; }
    public String getAssetId() { return assetId; }
    public String getAssetPath() { return assetPath; }

    public static class MobVariantId {
        private String datasetId;
        private String mobVariantId;

        public MobVariantId() {}
        public MobVariantId(String datasetId, String mobVariantId) { this.datasetId = datasetId; this.mobVariantId = mobVariantId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MobVariantId other)) return false;
            return datasetId.equals(other.datasetId) && mobVariantId.equals(other.mobVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + mobVariantId.hashCode(); }
    }
}
