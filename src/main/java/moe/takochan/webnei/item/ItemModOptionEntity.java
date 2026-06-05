package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemModOptionEntity.ModOptionId.class)
@Table(name = "v_item_mod_option")
public class ItemModOptionEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "mod_id", nullable = false)
    private String modId;

    @Column(nullable = false)
    private String name;

    protected ItemModOptionEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getModId() { return modId; }
    public String getName() { return name; }

    public static class ModOptionId {
        private String datasetId;
        private String modId;

        public ModOptionId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModOptionId other)) return false;
            return datasetId.equals(other.datasetId) && modId.equals(other.modId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + modId.hashCode(); }
    }
}
