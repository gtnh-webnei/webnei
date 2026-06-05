package moe.takochan.webnei.gtore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(GtOreVeinEntity.OreVeinId.class)
@Table(name = "gt_ore_vein")
public class GtOreVeinEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "vein_name", nullable = false)
    private String veinName;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private int size;

    @Column(nullable = false)
    private int density;

    @Column(name = "height_min", nullable = false)
    private int heightMin;

    @Column(name = "height_max", nullable = false)
    private int heightMax;

    protected GtOreVeinEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getVeinName() { return veinName; }
    public String getDisplayName() { return displayName; }
    public int getWeight() { return weight; }
    public int getSize() { return size; }
    public int getDensity() { return density; }
    public int getHeightMin() { return heightMin; }
    public int getHeightMax() { return heightMax; }

    public static class OreVeinId {
        private String datasetId;
        private String veinName;

        public OreVeinId() {}
        public OreVeinId(String datasetId, String veinName) {
            this.datasetId = datasetId;
            this.veinName = veinName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OreVeinId other)) return false;
            return datasetId.equals(other.datasetId) && veinName.equals(other.veinName);
        }

        @Override
        public int hashCode() { return datasetId.hashCode() * 31 + veinName.hashCode(); }
    }
}
