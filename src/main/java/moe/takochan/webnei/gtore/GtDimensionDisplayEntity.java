package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtDimensionDisplayEntity.DimensionDisplayId.class)
@Table(name = "gt_dimension_display")
public class GtDimensionDisplayEntity {
    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(nullable = false)
    private String dimension;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "display_abbr", nullable = false)
    private String displayAbbr;

    @Column(name = "icon_item_variant_id", nullable = false)
    private String iconItemVariantId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected GtDimensionDisplayEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getDimension() { return dimension; }
    public String getFullName() { return fullName; }
    public String getDisplayName() { return displayName; }
    public String getDisplayAbbr() { return displayAbbr; }
    public String getIconItemVariantId() { return iconItemVariantId; }
    public int getSortOrder() { return sortOrder; }

    public static class DimensionDisplayId {
        private String datasetId;
        private String dimension;

        public DimensionDisplayId() {}
        public DimensionDisplayId(String datasetId, String dimension) {
            this.datasetId = datasetId;
            this.dimension = dimension;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionDisplayId other)) return false;
            return datasetId.equals(other.datasetId) && dimension.equals(other.dimension);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + dimension.hashCode(); }
    }
}
