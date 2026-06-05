package moe.takochan.webnei.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemSearchEntity.ItemId.class)
@Table(name = "item")
public class ItemSearchEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_id", nullable = false)
    private String itemId;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    protected ItemSearchEntity() {}

    public static class ItemId {
        private String datasetId;
        private String itemId;

        public ItemId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemId other)) return false;
            return datasetId.equals(other.datasetId) && itemId.equals(other.itemId);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + itemId.hashCode(); }
    }
}
