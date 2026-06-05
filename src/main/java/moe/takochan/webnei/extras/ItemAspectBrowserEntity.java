package moe.takochan.webnei.extras;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(ItemAspectBrowserEntity.ItemAspectId.class)
@Table(name = "v_item_aspect_browser")
public class ItemAspectBrowserEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "item_variant_id", nullable = false)
    private String itemVariantId;

    @Id
    @Column(name = "aspect_id", nullable = false)
    private String aspectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean primal;

    @Column(nullable = false)
    private int amount;

    @Column(name = "icon_item_variant_id", nullable = false)
    private String iconItemVariantId;

    @Column(name = "icon_asset_path")
    private String iconAssetPath;

    @Column(name = "icon_asset_sha256")
    private String iconAssetSha256;

    protected ItemAspectBrowserEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getItemVariantId() { return itemVariantId; }
    public String getAspectId() { return aspectId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isPrimal() { return primal; }
    public int getAmount() { return amount; }
    public String getIconItemVariantId() { return iconItemVariantId; }
    public String getIconAssetPath() { return iconAssetPath; }
    public String getIconAssetSha256() { return iconAssetSha256; }

    public static class ItemAspectId {
        private String datasetId;
        private String itemVariantId;
        private String aspectId;

        public ItemAspectId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemAspectId other)) return false;
            return datasetId.equals(other.datasetId)
                    && itemVariantId.equals(other.itemVariantId)
                    && aspectId.equals(other.aspectId);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + itemVariantId.hashCode();
            result = 31 * result + aspectId.hashCode();
            return result;
        }
    }
}
