package moe.takochan.webnei.model.entity.view;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(FluidBlockBrowserEntity.FluidBlockId.class)
@Table(name = "v_fluid_block_browser")
public class FluidBlockBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "fluid_variant_id", nullable = false)
    private String fluidVariantId;

    @Id
    @Column(name = "block_item_variant_id", nullable = false)
    private String blockItemVariantId;

    protected FluidBlockBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getFluidVariantId() { return fluidVariantId; }
    public String getBlockItemVariantId() { return blockItemVariantId; }

    public static class FluidBlockId {
        private String datasetId;
        private String fluidVariantId;
        private String blockItemVariantId;

        public FluidBlockId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FluidBlockId other)) return false;
            return datasetId.equals(other.datasetId)
                    && fluidVariantId.equals(other.fluidVariantId)
                    && blockItemVariantId.equals(other.blockItemVariantId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + fluidVariantId.hashCode();
            result = 31 * result + blockItemVariantId.hashCode();
            return result;
        }
    }
}
