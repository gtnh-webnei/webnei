package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidContainerBrowserEntity.FluidContainerId.class)
@Table(name = "v_fluid_container_browser")
public class FluidContainerBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Id
    @Column(nullable = false)
    private int amount;

    @Id
    @Column(name = "container_item_variant_id", nullable = false)
    private String containerItemVariantId;

    @Id
    @Column(name = "empty_container_item_variant_id", nullable = false)
    private String emptyContainerItemVariantId;

    protected FluidContainerBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public int getAmount() { return amount; }
    public String getContainerItemVariantId() { return containerItemVariantId; }
    public String getEmptyContainerItemVariantId() { return emptyContainerItemVariantId; }

    public static class FluidContainerId {
        private String datasetId;
        private String fluidVariantId;
        private int amount;
        private String containerItemVariantId;
        private String emptyContainerItemVariantId;

        public FluidContainerId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidContainerId other)) return false;
            return amount == other.amount
                    && datasetId.equals(other.datasetId)
                    && fluidVariantId.equals(other.fluidVariantId)
                    && containerItemVariantId.equals(other.containerItemVariantId)
                    && emptyContainerItemVariantId.equals(other.emptyContainerItemVariantId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + fluidVariantId.hashCode();
            result = 31 * result + amount;
            result = 31 * result + containerItemVariantId.hashCode();
            result = 31 * result + emptyContainerItemVariantId.hashCode();
            return result;
        }
    }
}
