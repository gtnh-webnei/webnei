<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useEntityNavigation } from '@/composables/useEntityNavigation';

interface SpecialItemDisplay {
  itemVariantId: string;
  displayName: string | null;
  assetUrl: string | null;
}

defineProps<{
  items: SpecialItemDisplay[];
}>();

const router = useRouter();
const route = useRoute();
const datasetId = computed(() => String(route.params.datasetId ?? ''));
const entityNavigation = useEntityNavigation(router, datasetId);
const { t } = useI18n();

function onClick(item: SpecialItemDisplay) {
  entityNavigation.pick(item.itemVariantId);
}
</script>

<template>
  <section v-if="items.length" class="specials">
    <div class="label">
      {{ t('recipe.specialItems') }}
    </div>
    <div class="grid">
      <button
        v-for="item in items"
        :key="item.itemVariantId"
        type="button"
        class="cell"
        @click="onClick(item)"
      >
        <img
          v-if="item.assetUrl"
          :src="item.assetUrl"
          :alt="item.displayName ?? ''"
          loading="lazy"
        />
        <span v-else class="placeholder">?</span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.specials {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 32px);
  gap: 6px;
}
.cell {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  background: var(--el-fill-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.cell:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.cell img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
}
.placeholder {
  color: var(--el-text-color-secondary);
  font-weight: 700;
  font-size: 12px;
}
</style>
