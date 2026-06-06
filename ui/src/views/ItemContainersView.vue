<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { getItemContainers } from '@/api/extras';
import { getItemDetail } from '@/api/items';
import type { FluidContainerEntry } from '@/api/extras.types';
import type { ItemDetail } from '@/api/items.types';
import InteractiveFluidRef, {
  type InteractiveFluidRefFluid,
} from '@/components/InteractiveFluidRef.vue';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();

const datasetId = computed(() => String(route.params.datasetId ?? ''));
const itemVariantId = computed(() => String(route.params.itemVariantId ?? ''));

const item = ref<ItemDetail | null>(null);
const containers = ref<FluidContainerEntry[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const q = ref('');

const filtered = computed(() => {
  const needle = q.value.trim().toLowerCase();
  if (!needle) return containers.value;
  return containers.value.filter(
    (c) =>
      (c.fluidDisplayName ?? c.fluidVariantId).toLowerCase().includes(needle) ||
      c.fluidVariantId.toLowerCase().includes(needle) ||
      (c.containerDisplayName ?? '').toLowerCase().includes(needle) ||
      c.containerItemVariantId.toLowerCase().includes(needle) ||
      (c.emptyContainerDisplayName ?? '').toLowerCase().includes(needle) ||
      c.emptyContainerItemVariantId.toLowerCase().includes(needle),
  );
});

async function load() {
  if (!datasetId.value || !itemVariantId.value) return;
  loading.value = true;
  error.value = null;
  try {
    const [detailRes, containersRes] = await Promise.all([
      getItemDetail(datasetId.value, itemVariantId.value),
      getItemContainers(datasetId.value, itemVariantId.value),
    ]);
    item.value = detailRes;
    containers.value = containersRes;
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

function goToItem(variantId: string) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: variantId, kind: 'detail' },
  });
}

function fluidRefFromContainer(container: FluidContainerEntry): InteractiveFluidRefFluid {
  return {
    fluidVariantId: container.fluidVariantId,
    fluidId: container.fluidId,
    displayName: container.fluidDisplayName,
    assetUrl: container.fluidAssetUrl,
    gaseous: container.fluidGaseous,
    temperature: container.fluidTemperature,
    modName: container.fluidModName,
  };
}

function pickFluid(fluid: InteractiveFluidRefFluid) {
  if (!fluid.fluidVariantId) return;
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: fluid.fluidVariantId, kind: 'detail' },
  });
}

function lookupFluid(kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid) {
  if (!fluid.fluidVariantId) return;
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: fluid.fluidVariantId, kind },
  });
}

function back() {
  router.back();
}

watch([datasetId, itemVariantId], load);
onMounted(load);
</script>

<template>
  <div class="item-containers">
    <header class="header">
      <el-button text @click="back">{{ t('common.back') }}</el-button>
      <div class="title-row">
        <img v-if="item?.assetUrl" :src="item.assetUrl" class="hero-icon" :alt="item.displayName" />
        <div>
          <h1>
            {{ t('container.asFluidContainer', { name: item?.displayName ?? itemVariantId }) }}
          </h1>
          <p class="lead">
            <code class="id">{{ itemVariantId }}</code>
            <span class="lead-sep">·</span>
            {{ t('common.totalCount') }} {{ containers.length }} {{ t('container.totalMappings') }}
          </p>
        </div>
      </div>
    </header>

    <div class="toolbar">
      <el-input
        v-model="q"
        :placeholder="t('container.searchPlaceholder')"
        clearable
        class="search-input"
      />
      <div class="spacer" />
      <div class="total">
        {{ t('common.showing') }}<strong>{{ filtered.length }}</strong> / {{ containers.length }}
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading" :rows="8" animated />

    <el-table v-else :data="filtered" stripe class="container-table">
      <el-table-column
        :label="t('container.colFluid')"
        min-width="200"
        sortable
        :sort-by="(r: FluidContainerEntry) => r.fluidDisplayName ?? r.fluidVariantId"
      >
        <template #default="{ row }">
          <InteractiveFluidRef
            :fluid="fluidRefFromContainer(row)"
            variant="text"
            @pick="pickFluid"
            @lookup="lookupFluid"
          />
          <div class="sub">{{ row.fluidVariantId }}</div>
        </template>
      </el-table-column>
      <el-table-column :label="t('container.colFilledContainer')" min-width="220">
        <template #default="{ row }">
          <div class="cell-row" @click="goToItem(row.containerItemVariantId)">
            <img v-if="row.containerAssetUrl" :src="row.containerAssetUrl" />
            <div>
              <div class="link">{{ row.containerDisplayName ?? row.containerItemVariantId }}</div>
              <div class="sub">{{ row.containerItemVariantId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column
        :label="t('container.colCapacity')"
        width="110"
        align="right"
        sortable
        :sort-by="(r: FluidContainerEntry) => r.amount"
      >
        <template #default="{ row }">
          <span v-if="row.amount > 0" class="amount">{{ row.amount }} mB</span>
          <span v-else class="amount-zero">—</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('container.colEmptyContainer')" min-width="220">
        <template #default="{ row }">
          <div class="cell-row" @click="goToItem(row.emptyContainerItemVariantId)">
            <img v-if="row.emptyContainerAssetUrl" :src="row.emptyContainerAssetUrl" />
            <div>
              <div class="link">
                {{ row.emptyContainerDisplayName ?? row.emptyContainerItemVariantId }}
              </div>
              <div class="sub">{{ row.emptyContainerItemVariantId }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
export default { name: 'ItemContainersView' };
</script>

<style scoped>
.header {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hero-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  image-rendering: pixelated;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 4px;
}
.header h1 {
  margin: 0 0 4px 0;
  font-size: 20px;
}
.lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  display: flex;
  gap: 6px;
  align-items: center;
}
.lead-sep {
  opacity: 0.5;
}
.id {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 11px;
  background: var(--el-fill-color-light);
  padding: 1px 4px;
  border-radius: 3px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.search-input {
  width: 320px;
  max-width: 100%;
}
.spacer {
  flex: 1;
}
.total {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}
.total strong {
  color: var(--el-text-color-primary);
}
.container-table {
  width: 100%;
}
.cell-row {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.cell-row img {
  width: 28px;
  height: 28px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.link {
  color: var(--el-color-primary);
  cursor: pointer;
  font-size: 13px;
}
.link:hover {
  text-decoration: underline;
}
.sub {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  word-break: break-all;
}
.amount {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--el-color-primary);
}
.amount-zero {
  color: var(--el-text-color-secondary);
}
</style>
