package moe.takochan.webnei.recipe;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@IdClass(RecipeSlotLayoutEntity.SlotLayoutId.class)
@Table(name = "recipe_slot_layout")
public class RecipeSlotLayoutEntity {

    @Id
    @Column(name = "dataset_id", nullable = false)
    private String datasetId;

    @Id
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Id
    @Column(nullable = false)
    private String role;

    @Id
    @Column(name = "slot_index", nullable = false)
    private int slotIndex;

    @Column(nullable = false)
    private int x;

    @Column(nullable = false)
    private int y;

    @Column(nullable = false)
    private int width;

    @Column(nullable = false)
    private int height;

    @Column(name = "slot_style", nullable = false)
    private String slotStyle;

    @Column
    private String placement;

    protected RecipeSlotLayoutEntity() {}

    public String getDatasetId() { return datasetId; }
    public String getCategoryId() { return categoryId; }
    public String getRole() { return role; }
    public int getSlotIndex() { return slotIndex; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getSlotStyle() { return slotStyle; }
    public String getPlacement() { return placement; }

    public static class SlotLayoutId {
        private String datasetId;
        private String categoryId;
        private String role;
        private int slotIndex;

        public SlotLayoutId() {}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SlotLayoutId other)) return false;
            return slotIndex == other.slotIndex
                    && datasetId.equals(other.datasetId)
                    && categoryId.equals(other.categoryId)
                    && role.equals(other.role);
        }

        @Override
        public int hashCode() {
            int result = datasetId.hashCode();
            result = 31 * result + categoryId.hashCode();
            result = 31 * result + role.hashCode();
            result = 31 * result + slotIndex;
            return result;
        }
    }
}
