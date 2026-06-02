<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { RecycleScroller } from 'vue-virtual-scroller';
import { Loading } from '@element-plus/icons-vue';
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css';
import type { NeiPanelEntry } from '@/api/items.types';
import ItemIcon from './ItemIcon.vue';

const props = withDefaults(
  defineProps<{
    items: NeiPanelEntry[];
    loading?: boolean;
    iconSize?: number;
    gap?: number;
    canLoadMore?: boolean;
    total?: number;
    selectedId?: string | null;
  }>(),
  {
    loading: false,
    iconSize: 36,
    gap: 4,
    canLoadMore: false,
    total: 0,
    selectedId: null,
  },
);

const emit = defineEmits<{
  (e: 'select', item: NeiPanelEntry): void;
  (e: 'lookup', kind: 'recipe' | 'usage', item: NeiPanelEntry): void;
  (e: 'loadMore'): void;
}>();

const containerWidth = ref(0);
const containerRef = ref<HTMLElement | null>(null);
let resizeObs: ResizeObserver | null = null;

const cellSize = computed(() => props.iconSize + props.gap);
const columns = computed(() => {
  if (containerWidth.value <= 0) return 1;
  return Math.max(1, Math.floor(containerWidth.value / cellSize.value));
});

const rows = computed(() => {
  const cols = columns.value;
  if (cols <= 0 || props.items.length === 0) return [];
  const out: { id: number; cells: NeiPanelEntry[] }[] = [];
  for (let i = 0; i < props.items.length; i += cols) {
    out.push({ id: i, cells: props.items.slice(i, i + cols) });
  }
  return out;
});

watch(
  () => containerRef.value,
  (el) => {
    if (resizeObs) {
      resizeObs.disconnect();
      resizeObs = null;
    }
    if (!el) return;
    containerWidth.value = el.clientWidth;
    resizeObs = new ResizeObserver((entries) => {
      for (const entry of entries) {
        containerWidth.value = entry.contentRect.width;
      }
    });
    resizeObs.observe(el);
  },
  { immediate: true },
);

function onScroll(_event: Event, position: { startIndex: number; endIndex: number }) {
  if (!props.canLoadMore || props.loading) return;
  const lastRowIndex = rows.value.length - 1;
  if (position.endIndex >= lastRowIndex - 2) {
    emit('loadMore');
  }
}
</script>

<template>
  <div ref="containerRef" class="virtual-grid">
    <RecycleScroller
      v-if="columns > 0"
      class="scroller"
      :items="rows"
      :item-size="cellSize"
      key-field="id"
      @scroll="onScroll as never"
    >
      <template #default="{ item }">
        <div class="row" :style="{ height: cellSize + 'px', gap: gap + 'px' }">
          <ItemIcon
            v-for="cell in item.cells"
            :key="cell.itemVariantId"
            :item="cell"
            :size="iconSize"
            :class="{ selected: cell.itemVariantId === selectedId }"
            @click="emit('select', cell)"
            @lookup="(kind, it) => emit('lookup', kind, it)"
          />
        </div>
      </template>
    </RecycleScroller>
    <div v-if="loading" class="loading-footer">
      <el-icon class="is-loading"><Loading /></el-icon>
      {{ $t('common.loading') }}
    </div>
    <div v-else-if="!canLoadMore && items.length > 0" class="loading-footer muted">
      {{ $t('common.totalCount') }} {{ total }} {{ $t('common.totalSuffix') }}
    </div>
  </div>
</template>

<style scoped>
.virtual-grid {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  position: relative;
}
.scroller {
  flex: 1;
  min-height: 0;
}
.row {
  display: flex;
  align-items: center;
}
.row > .selected {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 1px;
}
.loading-footer {
  padding: 8px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
}
.loading-footer.muted {
  color: var(--el-text-color-disabled);
}
</style>
