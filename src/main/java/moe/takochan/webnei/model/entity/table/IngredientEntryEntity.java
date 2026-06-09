package moe.takochan.webnei.model.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(IngredientEntryEntity.IngredientEntryId.class)
@Table(name = "ingredient_entry")
public class IngredientEntryEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Id
    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Id
    @Column(nullable = false)
    private int amount;

    protected IngredientEntryEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getGroupId() { return groupId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public int getAmount() { return amount; }

    public static class IngredientEntryId {
        private String datasetId;
        private String groupId;
        private String itemVariantId;
        private String fluidVariantId;
        private int amount;

        public IngredientEntryId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IngredientEntryId other)) return false;
            return amount == other.amount
                    && datasetId.equals(other.datasetId)
                    && groupId.equals(other.groupId)
                    && itemVariantId.equals(other.itemVariantId)
                    && fluidVariantId.equals(other.fluidVariantId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + groupId.hashCode();
            result = 31 * result + itemVariantId.hashCode();
            result = 31 * result + fluidVariantId.hashCode();
            result = 31 * result + amount;
            return result;
        }
    }
}
