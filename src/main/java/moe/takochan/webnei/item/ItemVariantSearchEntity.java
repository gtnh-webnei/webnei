package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemVariantSearchEntity.ItemVariantId.class)
@Table(name = "item_variant")
public class ItemVariantSearchEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected ItemVariantSearchEntity() {}

    public static class ItemVariantId {
        private String datasetId;
        private String itemVariantId;

        public ItemVariantId() {}

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
