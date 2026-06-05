package moe.takochan.webnei.extras;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemOreDictionaryNameEntity.ItemOreDictionaryNameId.class)
@Table(name = "v_item_ore_dictionary_name")
public class ItemOreDictionaryNameEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Id
    @Column(name = "ore_name", nullable = false)
    private String oreName;

    protected ItemOreDictionaryNameEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getOreName() { return oreName; }

    public static class ItemOreDictionaryNameId {
        private String datasetId;
        private String itemVariantId;
        private String oreName;

        public ItemOreDictionaryNameId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemOreDictionaryNameId other)) return false;
            return datasetId.equals(other.datasetId)
                    && itemVariantId.equals(other.itemVariantId)
                    && oreName.equals(other.oreName);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + itemVariantId.hashCode();
            result = 31 * result + oreName.hashCode();
            return result;
        }
    }
}
