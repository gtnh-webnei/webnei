<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useDebounceFn } from '@vueuse/core';
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import { useDatasetStore } from '@/stores/dataset';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import AppTooltip from '@/components/AppTooltip.vue';
import SlotCell from '@/components/SlotCell.vue';
import type { FluidRef, ItemRef } from '@/api/entityRefs.types';
import InteractiveItemRef from '@/components/InteractiveItemRef.vue';
import InteractiveFluidRef from '@/components/InteractiveFluidRef.vue';
import {
  getGtBartWorksOreDetail,
  getGtOreVeinDetail,
  getGtSmallOreDetail,
  getGtUndergroundFluidDetail,
  listGtBartWorksOres,
  listGtOreVeins,
  listGtSmallOres,
  listGtUndergroundFluids,
} from '@/api/gt';
import type {
  GtBartWorksOreDetail,
  GtBartWorksOreSummary,
  GtDimensionRef,
  GtFluidRef,
  GtItemRef,
  GtOreVeinDetail,
  GtOreVeinSummary,
  GtSection,
  GtSmallOreDetail,
  GtSmallOreSummary,
  GtUndergroundFluidDetail,
  GtUndergroundFluidSummary,
} from '@/api/gt.types';
import type { RecipeSlot } from '@/api/recipes.types';
import { formatChancePercent } from '@/utils/format';

const { t } = useI18n();
const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);

const sections = computed<{ key: GtSection; label: string }[]>(() => [
  { key: 'ore-veins', label: t('gtResource.oreVeins') },
  { key: 'small-ores', label: t('gtResource.smallOres') },
  { key: 'underground-fluids', label: t('gtResource.undergroundFluids') },
  { key: 'bartworks-ores', label: t('gtResource.bartWorksOres') },
]);

type Summary =
  | GtOreVeinSummary
  | GtSmallOreSummary
  | GtUndergroundFluidSummary
  | GtBartWorksOreSummary;
type Detail = GtOreVeinDetail | GtSmallOreDetail | GtUndergroundFluidDetail | GtBartWorksOreDetail;

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));
const entityNavigation = useEntityNavigation(router, datasetId);
const activeSection = computed<GtSection>(() => {
  const raw = String(route.params.section ?? 'ore-veins');
  return sections.value.some((s) => s.key === raw) ? (raw as GtSection) : 'ore-veins';
});
const q = ref('');
const dimension = ref('');
const type = ref('');
const items = ref<Summary[]>([]);
const dimensionOptions = ref<GtDimensionRef[]>([]);
const typeOptions = ref<string[]>([]);
const total = computed(() => items.value.length);
const selectedKey = ref('');
const detail = ref<Detail | null>(null);
const loading = ref(false);
const detailLoading = ref(false);
const error = ref<string | null>(null);
const showDetailOnMobile = ref(false);
let listRequestId = 0;
let detailRequestId = 0;

function gotoSection(section: GtSection) {
  router.push({ name: 'gt', params: { datasetId: datasetId.value, section } });
}

function itemKey(item: Summary): string {
  if ('veinName' in item) return item.veinName;
  if ('oreGenName' in item) return item.oreGenName;
  if ('fluidId' in item) return item.fluidId;
  return item.entryId;
}

function targetKeyFromQuery(): string | null {
  const target = typeof route.query.target === 'string' ? route.query.target : '';
  if (target) return target;
  if (activeSection.value !== 'underground-fluids') return null;
  const fluidId = typeof route.query.fluidId === 'string' ? route.query.fluidId : '';
  return fluidId || null;
}

function itemTitle(item: Summary): string {
  if ('veinName' in item) return item.displayName || item.veinName;
  if ('oreGenName' in item) return formatItemName(item.smallOreItem);
  if ('fluid' in item) return item.fluid.displayName || item.fluidId;
  return item.resultItem.displayName || item.entryId;
}

function itemIcon(item: Summary): string | null {
  if ('veinName' in item) return item.primaryItem?.assetUrl ?? null;
  if ('oreGenName' in item) return item.smallOreItem.assetUrl;
  if ('fluid' in item) return item.fluid.assetUrl;
  return item.resultItem.assetUrl;
}

function itemDimensions(item: Summary): GtDimensionRef[] {
  if ('dimensions' in item) return item.dimensions;
  return [
    {
      dimension: item.dimension,
      fullName: item.dimensionDisplayName,
      displayName: item.dimensionDisplayName,
      displayAbbr: item.dimensionDisplayName,
      iconItemVariantId: '',
      iconAssetUrl: null,
      sortOrder: Number.MAX_SAFE_INTEGER,
    },
  ];
}

function currentDetailKind() {
  if (!detail.value) return null;
  if ('fluid' in detail.value) return 'underground-fluid';
  if ('smallOreItem' in detail.value) return 'small-ore';
  if ('resultItem' in detail.value) return 'bartworks-ore';
  return 'ore-vein';
}

async function fetchList() {
  if (!datasetId.value) return;
  const current = ++listRequestId;
  loading.value = true;
  error.value = null;
  try {
    const params = {
      q: q.value,
      dimension: dimension.value || undefined,
    };
    const data =
      activeSection.value === 'ore-veins'
        ? await listGtOreVeins(datasetId.value, params)
        : activeSection.value === 'small-ores'
          ? await listGtSmallOres(datasetId.value, params)
          : activeSection.value === 'underground-fluids'
            ? await listGtUndergroundFluids(datasetId.value, params)
            : await listGtBartWorksOres(datasetId.value, {
                ...params,
                type: type.value || undefined,
              });
    if (current !== listRequestId) return;
    items.value = data.items;
    dimensionOptions.value = data.dimensions;
    typeOptions.value = data.types;
    const querySelectedKey = targetKeyFromQuery();
    const selected =
      (querySelectedKey ? items.value.find((item) => itemKey(item) === querySelectedKey) : null) ??
      items.value.find((item) => itemKey(item) === selectedKey.value) ??
      items.value[0];
    selectedKey.value = selected ? itemKey(selected) : '';
    if (selected) await fetchDetail(selected);
    else detail.value = null;
  } catch (e) {
    if (current !== listRequestId) return;
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    if (current === listRequestId) loading.value = false;
  }
}

async function fetchDetail(item: Summary) {
  if (!datasetId.value) return;
  const current = ++detailRequestId;
  detailLoading.value = true;
  try {
    if ('veinName' in item) detail.value = await getGtOreVeinDetail(datasetId.value, item.veinName);
    else if ('oreGenName' in item)
      detail.value = await getGtSmallOreDetail(datasetId.value, item.oreGenName);
    else if ('fluid' in item) {
      detail.value = await getGtUndergroundFluidDetail(datasetId.value, item.fluidId);
    } else detail.value = await getGtBartWorksOreDetail(datasetId.value, item.entryId);
    if (current === detailRequestId) showDetailOnMobile.value = true;
  } catch (e) {
    if (current === detailRequestId) error.value = e instanceof Error ? e.message : String(e);
  } finally {
    if (current === detailRequestId) detailLoading.value = false;
  }
}

function selectItem(item: Summary) {
  selectedKey.value = itemKey(item);
  fetchDetail(item);
}

const debouncedFetch = useDebounceFn(() => fetchList(), 250);

watch([datasetId, activeSection], () => {
  q.value = '';
  dimension.value = '';
  type.value = '';
  showDetailOnMobile.value = false;
  fetchList();
});
watch([q, dimension, type], () => debouncedFetch());

onMounted(() => fetchList());

function formatItemName(item: GtItemRef | null | undefined) {
  return item?.displayName || item?.itemVariantId || '—';
}
function formatFluidName(fluid: GtFluidRef | null | undefined) {
  return fluid?.displayName || fluid?.fluidVariantId || '—';
}
function detailTitle(value: Detail) {
  if ('displayName' in value) return value.displayName;
  if ('smallOreItem' in value) return formatItemName(value.smallOreItem);
  if ('fluid' in value) return formatFluidName(value.fluid);
  return formatItemName(value.resultItem);
}
function detailIcon(value: Detail) {
  if ('displayName' in value)
    return value.layers.find((l) => l.layer === 'primary')?.item.assetUrl ?? null;
  if ('smallOreItem' in value) return value.smallOreItem.assetUrl;
  if ('fluid' in value) return value.fluid.assetUrl;
  return value.resultItem.assetUrl;
}
function detailStats(value: Detail): { label: string; value: string }[] {
  if ('displayName' in value) {
    return [
      {
        label: t('gtResource.statGenerationHeight'),
        value: `${value.heightMin}-${value.heightMax}`,
      },
      { label: t('gtResource.statGenerationWeight'), value: String(value.weight) },
    ];
  }
  if ('smallOreItem' in value) {
    return [
      {
        label: t('gtResource.statGenerationHeight'),
        value: `${value.heightMin}-${value.heightMax}`,
      },
      { label: t('gtResource.statAmountPerChunk'), value: String(value.amountPerChunk) },
    ];
  }
  if ('fluid' in value) return [];
  if (value.entryType === 'small') {
    return [
      { label: t('gtResource.statType'), value: value.entryType },
      { label: t('gtResource.dimension'), value: value.dimensionDisplayName },
      {
        label: t('gtResource.statGenerationHeight'),
        value: `${value.heightMin}-${value.heightMax}`,
      },
      { label: t('gtResource.statAmountPerChunk'), value: String(value.amountPerChunk) },
    ];
  }
  return [
    { label: t('gtResource.statType'), value: value.entryType },
    { label: t('gtResource.dimension'), value: value.dimensionDisplayName },
    { label: t('gtResource.statGenerationHeight'), value: `${value.heightMin}-${value.heightMax}` },
    { label: t('gtResource.statGenerationWeight'), value: String(value.weight) },
    { label: t('gtResource.statGenerationSize'), value: String(value.size) },
    { label: t('gtResource.statGenerationDensity'), value: String(value.density) },
  ];
}
function detailDimensions(value: Detail): GtDimensionRef[] {
  if ('dimensions' in value) return value.dimensions;
  return [];
}
function layerLabel(layer: string) {
  const labels: Record<string, string> = {
    primary: t('gtResource.layerPrimary'),
    secondary: t('gtResource.layerSecondary'),
    between: t('gtResource.layerBetween'),
    sporadic: t('gtResource.layerSporadic'),
  };
  return labels[layer] ?? layer;
}
function layerOrder(layer: string) {
  const order: Record<string, number> = { primary: 0, secondary: 1, between: 2, sporadic: 3 };
  return order[layer] ?? 99;
}
function orderedOreVeinLayers(value: GtOreVeinDetail) {
  return [...value.layers].sort((a, b) => layerOrder(a.layer) - layerOrder(b.layer));
}
function bartWorksLayerLabel(detail: GtBartWorksOreDetail, layer: string) {
  if (detail.entryType === 'small') return t('gtResource.layerSporadic');
  return layerLabel(layer);
}
function slotFromItem(item: GtItemRef): RecipeSlot {
  return {
    role: 'gt_item',
    slotIndex: 0,
    itemVariantId: item.itemVariantId,
    fluidVariantId: null,
    amount: 0,
    probability: 1,
    groupId: null,
    displayName: item.displayName,
    modId: null,
    modName: null,
    fluidGaseous: null,
    fluidTemperature: null,
    tooltipText: item.tooltipText,
    assetUrl: item.assetUrl,
    candidates: [],
  };
}
function pickSlot(payload: { itemVariantId: string | null; fluidVariantId: string | null }) {
  entityNavigation.pick(payload);
}
function lookupSlot(
  kind: 'recipe' | 'usage',
  payload: { itemVariantId: string | null; fluidVariantId: string | null },
) {
  entityNavigation.lookup(kind, payload);
}
function pickItem(item: ItemRef) {
  entityNavigation.pick(item.itemVariantId);
}
function lookupItem(kind: 'recipe' | 'usage', item: ItemRef) {
  entityNavigation.lookup(kind, item.itemVariantId);
}
function pickFluid(fluid: FluidRef) {
  entityNavigation.pick(fluid.fluidVariantId);
}
function lookupFluid(kind: 'recipe' | 'usage', fluid: FluidRef) {
  entityNavigation.lookup(kind, fluid.fluidVariantId);
}
</script>

<template>
  <div class="gt-view">
    <header class="gt-header">
      <div class="gt-heading">
        <h1>{{ t('gtResource.pageTitle') }}</h1>
      </div>
      <nav
        class="page-tabs gt-section-tabs"
        :aria-label="t('gtResource.sectionMenuLabel')"
      >
        <button
          v-for="section in sections"
          :key="section.key"
          type="button"
          class="page-tab"
          :class="{ active: activeSection === section.key }"
          @click="gotoSection(section.key)"
        >
          {{ section.label }}
        </button>
      </nav>
    </header>

    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />

    <section
      class="gt-browser"
      :class="{ 'show-detail': showDetailOnMobile }"
    >
      <aside
        v-loading="loading"
        class="gt-list-card"
      >
        <div class="list-toolbar">
          <el-input
            v-model="q"
            clearable
            :placeholder="t('gtResource.searchPlaceholder')"
            class="search-input"
          />
          <el-select
            v-model="dimension"
            clearable
            filterable
            :placeholder="t('gtResource.dimensionFilter')"
            class="filter-input"
          >
            <el-option
              v-for="option in dimensionOptions"
              :key="option.dimension"
              :label="option.displayName"
              :value="option.dimension"
            >
              <span class="dimension-option">
                <span class="dimension-option-icon">
                  <img
                    v-if="option.iconAssetUrl"
                    :src="option.iconAssetUrl"
                    :alt="option.displayName"
                  >
                </span>
                <span>{{ option.displayName }}</span>
              </span>
            </el-option>
          </el-select>
          <el-select
            v-if="activeSection === 'bartworks-ores'"
            v-model="type"
            clearable
            :placeholder="t('gtResource.typeFilter')"
            class="filter-input"
          >
            <el-option
              v-for="option in typeOptions"
              :key="option"
              :label="option"
              :value="option"
            />
          </el-select>
        </div>

        <div class="list-scroll">
          <el-empty
            v-if="!loading && items.length === 0"
            :description="t('gtResource.empty')"
          />
          <button
            v-for="item in items"
            v-else
            :key="itemKey(item)"
            class="result-card"
            :class="{ active: itemKey(item) === selectedKey }"
            @click="selectItem(item)"
          >
            <span class="icon-box">
              <img
                v-if="itemIcon(item)"
                :src="itemIcon(item)!"
                :alt="itemTitle(item)"
              >
            </span>
            <span class="card-main">
              <span class="card-title">{{ itemTitle(item) }}</span>
              <span
                v-if="itemDimensions(item).length"
                class="dimension-row"
              >
                <span
                  v-for="dim in itemDimensions(item).slice(0, 6)"
                  :key="dim.dimension"
                  class="dimension-icon"
                >
                  <img
                    v-if="dim.iconAssetUrl"
                    :src="dim.iconAssetUrl"
                    :alt="dim.displayName"
                  >
                  <span
                    v-else
                    class="dimension-text"
                  >{{ dim.displayName }}</span>
                </span>
              </span>
            </span>
          </button>
        </div>

        <div class="list-footer">
          <span>{{ t('common.totalCount') }} {{ total.toLocaleString() }} {{ t('common.items') }}</span>
        </div>
      </aside>

      <main
        v-loading="detailLoading"
        class="gt-detail"
      >
        <el-button
          class="mobile-back"
          text
          @click="showDetailOnMobile = false"
        >
          {{ t('common.back') }}
        </el-button>
        <el-empty
          v-if="!detail"
          :description="t('gtResource.selectItemEmpty')"
        />

        <template v-else>
          <section class="detail-hero-card">
            <div class="detail-icon-box">
              <img
                v-if="detailIcon(detail)"
                :src="detailIcon(detail)!"
                :alt="detailTitle(detail)"
              >
            </div>
            <div class="detail-hero-main">
              <h2>{{ detailTitle(detail) }}</h2>
              <div
                v-if="
                  currentDetailKind() === 'underground-fluid' ||
                    currentDetailKind() === 'bartworks-ore'
                "
                class="detail-tags"
              >
                <el-tag
                  v-if="currentDetailKind() === 'underground-fluid'"
                  effect="plain"
                  round
                >
                  {{ t('gtResource.undergroundFluidTag') }}
                </el-tag>
                <el-tag
                  v-if="currentDetailKind() === 'bartworks-ore'"
                  effect="plain"
                  round
                >
                  {{ t('gtResource.bartWorksTag') }}
                </el-tag>
              </div>
              <dl
                v-if="detailStats(detail).length"
                class="stats-grid"
              >
                <div
                  v-for="stat in detailStats(detail)"
                  :key="stat.label"
                >
                  <dt>{{ stat.label }}</dt>
                  <dd>{{ stat.value }}</dd>
                </div>
              </dl>
              <section
                v-if="detailDimensions(detail).length"
                class="dimension-strip"
              >
                <h3>{{ t('gtResource.dimension') }}</h3>
                <div class="dimension-detail-row">
                  <AppTooltip
                    v-for="dim in detailDimensions(detail)"
                    :key="dim.dimension"
                    placement="top"
                  >
                    <template #content>
                      <div class="dimension-tooltip">
                        <div class="dimension-tooltip-name">
                          {{ dim.displayName }}
                        </div>
                        <div class="dimension-tooltip-sub">
                          {{ dim.fullName }}
                        </div>
                      </div>
                    </template>
                    <span class="detail-dimension-icon">
                      <img
                        v-if="dim.iconAssetUrl"
                        :src="dim.iconAssetUrl"
                        :alt="dim.displayName"
                      >
                    </span>
                  </AppTooltip>
                </div>
              </section>
            </div>
          </section>

          <section
            v-if="currentDetailKind() === 'ore-vein'"
            class="layer-grid"
          >
            <article
              v-for="layer in orderedOreVeinLayers(detail as GtOreVeinDetail)"
              :key="layer.layer"
              class="layer-card"
            >
              <h3 class="layer-title">
                <span class="layer-title-icon">
                  <img
                    v-if="layer.item.assetUrl"
                    :src="layer.item.assetUrl"
                    :alt="layer.item.displayName ?? layer.item.itemVariantId"
                  >
                </span>
                <span>{{ layerLabel(layer.layer) }}</span>
              </h3>
              <span class="layer-item-name">{{
                layer.item.displayName || layer.item.itemVariantId
              }}</span>
              <div class="variant-grid">
                <SlotCell
                  v-for="variant in layer.variants.slice(0, 18)"
                  :key="variant.itemVariantId"
                  v-bind="{ slot: slotFromItem(variant), size: 32 }"
                  @pick="pickSlot"
                  @lookup="lookupSlot"
                />
              </div>
            </article>
          </section>

          <template v-else-if="currentDetailKind() === 'small-ore'">
            <section class="detail-section-card">
              <h3>{{ t('gtResource.relatedItem') }}</h3>
              <InteractiveItemRef
                :item="(detail as GtSmallOreDetail).smallOreItem"
                variant="row"
                @pick="pickItem"
                @lookup="lookupItem"
              />
            </section>
            <section class="detail-section-card">
              <h3>{{ t('gtResource.chanceDrops') }}</h3>
              <div class="variant-grid named">
                <InteractiveItemRef
                  :item="(detail as GtSmallOreDetail).dustItem"
                  variant="row"
                  @pick="pickItem"
                  @lookup="lookupItem"
                />
                <InteractiveItemRef
                  v-for="drop in (detail as GtSmallOreDetail).drops"
                  :key="drop.itemVariantId"
                  :item="drop"
                  variant="row"
                  @pick="pickItem"
                  @lookup="lookupItem"
                />
              </div>
            </section>
          </template>

          <template v-else-if="currentDetailKind() === 'underground-fluid'">
            <section class="detail-section-card">
              <h3>{{ t('gtResource.relatedFluid') }}</h3>
              <InteractiveFluidRef
                :fluid="(detail as GtUndergroundFluidDetail).fluid"
                variant="row"
                @pick="pickFluid"
                @lookup="lookupFluid"
              />
            </section>
            <section class="detail-section-card underground-fluid-table">
              <div class="underground-fluid-head">
                <span>{{ t('gtResource.dimension') }}</span>
                <span>{{ t('gtResource.statChance') }}</span>
                <span>{{ t('gtResource.statReserve') }}</span>
              </div>
              <div
                v-for="entry in (detail as GtUndergroundFluidDetail).entries"
                :key="entry.dimension"
                class="underground-fluid-row"
              >
                <span class="underground-dimension-cell">
                  <span class="detail-dimension-icon compact">
                    <img
                      v-if="entry.dimensionDisplay.iconAssetUrl"
                      :src="entry.dimensionDisplay.iconAssetUrl"
                      :alt="entry.dimensionDisplay.displayName"
                    >
                  </span>
                  <span>{{ entry.dimensionDisplay.displayName }}</span>
                </span>
                <span>{{ formatChancePercent(entry.chance) }}</span>
                <span>{{ entry.minAmount }}-{{ entry.maxAmount }} L</span>
              </div>
            </section>
          </template>

          <section
            v-else-if="currentDetailKind() === 'bartworks-ore'"
            class="bartworks-card"
          >
            <div class="bartworks-header">
              <h3>
                {{
                  (detail as GtBartWorksOreDetail).entryType === 'small'
                    ? t('gtResource.bartWorksSmallOreGeneration')
                    : t('gtResource.bartWorksVeinGeneration')
                }}
              </h3>
            </div>
            <div class="bartworks-layers">
              <div
                v-for="layer in (detail as GtBartWorksOreDetail).layers"
                :key="`${layer.layer}|${layer.layerIndex}`"
                class="bartworks-layer-row"
              >
                <span class="bartworks-layer-label">{{
                  bartWorksLayerLabel(detail as GtBartWorksOreDetail, layer.layer)
                }}</span>
                <InteractiveItemRef
                  :item="layer.item"
                  variant="row"
                  @pick="pickItem"
                  @lookup="lookupItem"
                />
              </div>
            </div>
          </section>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped>
.gt-view {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 16px;
  height: calc(100vh - 96px);
  min-height: 0;
  overflow: hidden;
  color: var(--el-text-color-primary);
}
.gt-header {
  display: grid;
  grid-template-rows: auto auto;
  gap: 12px;
  align-items: start;
}
.gt-section-tabs {
  justify-content: start;
  min-width: 0;
  padding-bottom: 2px;
}
.gt-heading h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
}
.gt-heading p {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 15px;
}
.gt-browser {
  display: grid;
  grid-template-columns: minmax(320px, 380px) minmax(0, 1fr);
  gap: 18px;
  min-height: 0;
}
.gt-list-card,
.gt-detail {
  min-height: 0;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 14px;
  background: var(--el-bg-color);
  box-shadow: var(--el-box-shadow-light);
}
.gt-list-card {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 10px;
  padding: 10px;
  overflow: hidden;
}
.list-toolbar {
  display: grid;
  gap: 8px;
}
.search-input,
.filter-input {
  min-width: 0;
}
.dimension-option {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  min-width: 0;
}
.dimension-option-icon {
  width: 22px;
  height: 22px;
  display: inline-grid;
  place-items: center;
}
.dimension-option-icon img {
  width: 16px;
  height: 16px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.list-scroll {
  display: grid;
  align-content: start;
  gap: 6px;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}
.result-card {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  width: 100%;
  min-height: 72px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 9px;
  background: var(--el-bg-color);
  color: inherit;
  padding: 8px;
  text-align: left;
  cursor: pointer;
}
.result-card:hover,
.result-card.active {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.icon-box {
  width: 40px;
  height: 40px;
  display: inline-grid;
  place-items: center;
  border-radius: 8px;
  background: var(--el-fill-color-lighter);
}
.icon-box img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.card-main {
  display: grid;
  grid-template-rows: 20px 28px;
  gap: 5px;
  align-content: center;
  min-width: 0;
}
.card-title,
.card-sub {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-title {
  font-size: 14px;
  font-weight: 650;
  line-height: 1.25;
}
.card-sub {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.dimension-row,
.dimension-detail-row {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 5px;
  align-items: center;
  overflow: hidden;
}
.dimension-row {
  height: 28px;
}
.dimension-icon {
  box-sizing: border-box;
  min-width: 26px;
  width: auto;
  height: 26px;
  display: inline-grid;
  place-items: center;
}
.dimension-icon img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.dimension-text {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.list-footer {
  display: grid;
  gap: 6px;
  justify-items: center;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.gt-detail {
  display: grid;
  align-content: start;
  gap: 16px;
  padding: 18px;
  overflow: auto;
}
.mobile-back {
  display: none;
}
.detail-hero-card,
.detail-section-card,
.layer-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color);
}
.detail-hero-card {
  display: grid;
  grid-template-columns: 128px minmax(0, 1fr);
  gap: 24px;
  padding: 22px;
}
.detail-icon-box {
  width: 112px;
  height: 112px;
  display: grid;
  place-items: center;
  align-self: start;
  border-radius: 14px;
  background: var(--el-fill-color-lighter);
}
.detail-icon-box img {
  width: 96px;
  height: 96px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.detail-hero-main {
  display: grid;
  gap: 14px;
  min-width: 0;
}
.detail-hero-main h2 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
}
.detail-tags {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 6px;
  overflow: hidden;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 10px;
  margin: 0;
}
.stats-grid div {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-fill-color-light);
  padding: 10px 12px;
}
.stats-grid dt {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.stats-grid dd {
  margin: 4px 0 0;
  color: var(--el-color-primary);
  font-size: 17px;
  font-weight: 700;
}
.dimension-strip {
  display: grid;
  grid-template-columns: max-content minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 12px;
}
.dimension-strip h3 {
  margin: 0;
  font-size: 15px;
}
.detail-dimension-icon {
  width: 36px;
  height: 36px;
  display: inline-grid;
  place-items: center;
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
  border: 1px solid var(--el-border-color-lighter);
}
.detail-dimension-icon img {
  width: 30px;
  height: 30px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.dimension-tooltip {
  display: grid;
  gap: 3px;
  max-width: 220px;
}
.dimension-tooltip-name {
  font-size: 13px;
  font-weight: 600;
}
.dimension-tooltip-sub {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  word-break: break-word;
}
.layer-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.layer-card,
.detail-section-card {
  display: grid;
  gap: 12px;
  align-content: start;
  padding: 16px;
}
.layer-card {
  position: relative;
  overflow: hidden;
}
.layer-card h3,
.detail-section-card h3 {
  display: grid;
  grid-template-columns: max-content minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  margin: 0;
  color: var(--el-color-primary);
  font-size: 18px;
  line-height: 1.25;
}
.layer-title-icon {
  width: 32px;
  height: 32px;
  display: inline-grid;
  place-items: center;
}
.layer-title-icon img {
  width: 32px;
  height: 32px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.layer-item-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-primary);
  font-size: 16px;
}
.item-pair {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.underground-fluid-table {
  gap: 0;
  padding: 0;
  overflow: hidden;
}
.underground-fluid-head,
.underground-fluid-row {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) 100px 140px;
  gap: 12px;
  align-items: center;
  padding: 10px 14px;
}
.underground-fluid-head {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 650;
}
.underground-fluid-row + .underground-fluid-row {
  border-top: 1px solid var(--el-border-color-lighter);
}
.underground-dimension-cell {
  display: grid;
  grid-template-columns: 32px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  min-width: 0;
}
.underground-dimension-cell > span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-dimension-icon.compact {
  width: 28px;
  height: 28px;
}
.detail-dimension-icon.compact img {
  width: 24px;
  height: 24px;
}
.variant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 32px);
  gap: 8px;
}
.variant-grid.named {
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
}
.bartworks-card {
  display: grid;
  gap: 14px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  background: var(--el-bg-color);
  padding: 16px;
}
.bartworks-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  gap: 8px;
  align-items: center;
}
.bartworks-header h3 {
  margin: 0;
  color: var(--el-color-primary);
  font-size: 18px;
}
.bartworks-result,
.bartworks-layer-row {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
}
.bartworks-label,
.bartworks-layer-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-weight: 600;
}
.bartworks-layers {
  display: grid;
  gap: 8px;
}
@media (max-width: 760px) {
  .gt-header,
  .gt-browser,
  .detail-hero-card,
  .layer-grid,
  .item-pair,
  .stats-grid,
  .dimension-strip {
    grid-template-columns: minmax(0, 1fr);
  }
  .gt-browser.show-detail .gt-list-card {
    display: none;
  }
  .gt-browser:not(.show-detail) .gt-detail {
    display: none;
  }
  .mobile-back {
    display: inline-grid;
    justify-self: start;
  }
}
</style>
