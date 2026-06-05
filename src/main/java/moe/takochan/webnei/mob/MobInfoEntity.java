package moe.takochan.webnei.mob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(MobInfoEntity.MobInfoId.class)
@Table(name = "mob_info")
public class MobInfoEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mob_variant_id", nullable = false)
    private String mobVariantId;

    @Column(name = "allowed_in_peaceful", nullable = false)
    private boolean allowedInPeaceful;

    @Column(name = "soul_vial_usable", nullable = false)
    private boolean soulVialUsable;

    @Column(name = "allowed_infernal", nullable = false)
    private boolean allowedInfernal;

    @Column(name = "always_infernal", nullable = false)
    private boolean alwaysInfernal;

    protected MobInfoEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getMobVariantId() { return mobVariantId; }
    public boolean isAllowedInPeaceful() { return allowedInPeaceful; }
    public boolean isSoulVialUsable() { return soulVialUsable; }
    public boolean isAllowedInfernal() { return allowedInfernal; }
    public boolean isAlwaysInfernal() { return alwaysInfernal; }

    public static class MobInfoId {
        private String datasetId;
        private String mobVariantId;

        public MobInfoId() {}
        public MobInfoId(String datasetId, String mobVariantId) {
            this.datasetId = datasetId;
            this.mobVariantId = mobVariantId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MobInfoId other)) return false;
            return datasetId.equals(other.datasetId) && mobVariantId.equals(other.mobVariantId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + mobVariantId.hashCode(); }
    }
}
