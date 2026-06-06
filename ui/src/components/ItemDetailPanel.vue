<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { useExtrasStore } from '@/stores/extras';
import { getItemDetail } from '@/api/items';
import type { ItemDetail } from '@/api/items.types';
import type { FluidContainerEntry, ItemExtras } from '@/api/extras.types';
import MinecraftTooltipText from './MinecraftTooltipText.vue';
import InteractiveFluidRef, { type InteractiveFluidRefFluid } from './InteractiveFluidRef.vue';

const props = defineProps<{
  datasetId: string;
  itemVariantId: string;
}>();

const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);
void activeDatasetId;
const extrasStore = useExtrasStore();
const { t } = useI18n();

const detail = ref<ItemDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const extras = ref<ItemExtras | null>(null);
const extrasLoading = ref(false);
const extrasError = ref<string | null>(null);

const hasAnyExtras = computed(() => {
  if (!extras.value) return false;
  return (
    extras.value.oreDictNames.length > 0 ||
    extras.value.fluidContainers.length > 0 ||
    extras.value.aspects.length > 0 ||
    extras.value.asInputRecipeCount > 0 ||
    extras.value.asOutputRecipeCount > 0
  );
});

async function load() {
  if (!props.datasetId || !props.itemVariantId) return;
  loading.value = true;
  error.value = null;
  detail.value = null;
  try {
    detail.value = await getItemDetail(props.datasetId, props.itemVariantId);
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

async function loadExtras() {
  if (!props.datasetId || !props.itemVariantId) return;
  extrasLoading.value = true;
  extrasError.value = null;
  extras.value = null;
  try {
    extras.value = await extrasStore.loadItem(props.datasetId, props.itemVariantId);
  } catch (e) {
    extrasError.value = e instanceof Error ? e.message : String(e);
  } finally {
    extrasLoading.value = false;
  }
}

function copyId() {
  if (!detail.value) return;
  navigator.clipboard?.writeText(detail.value.itemVariantId);
}

function goLookup(kind: 'recipe' | 'usage', target?: string) {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: target ?? props.itemVariantId, kind },
  });
}

function goToItem(itemVariantId: string) {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: itemVariantId, kind: 'detail' },
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
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: fluid.fluidVariantId, kind: 'detail' },
  });
}

function lookupFluid(kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid) {
  if (!fluid.fluidVariantId) return;
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: fluid.fluidVariantId, kind },
  });
}

function goToContainers() {
  router.push({
    name: 'item-containers',
    params: { datasetId: props.datasetId, itemVariantId: props.itemVariantId },
  });
}

function goToWorldGeneration(section: string, key: string) {
  router.push({
    name: 'gt',
    params: { datasetId: props.datasetId, section },
    query: { target: key },
  });
}

watch(
  () => [props.datasetId, props.itemVariantId],
  () => {
    load();
    loadExtras();
  },
);
onMounted(() => {
  load();
  loadExtras();
});
</script>

<template>
  <div class="item-detail">
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading" :rows="8" animated />

    <div v-else-if="detail" class="content">
      <header class="hero">
        <div class="icon-wrap">
          <img
            v-if="detail.assetUrl"
            :src="detail.assetUrl"
            :alt="detail.displayName"
            class="hero-icon"
          />
        </div>
        <div class="title-block">
          <h1 class="title">{{ detail.displayName || detail.registryName }}</h1>
          <div class="subtitle">
            <el-tag size="default" type="info" effect="plain" round>{{ detail.modName }}</el-tag>
          </div>
          <div class="actions">
            <el-button type="primary" @click="goLookup('recipe')">{{
              $t('common.viewRecipe')
            }}</el-button>
            <el-button @click="goLookup('usage')">{{ $t('common.viewUsage') }}</el-button>
          </div>
        </div>
      </header>

      <el-row :gutter="16">
        <el-col :xs="24" :md="14">
          <el-card shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('common.basicAttributes') }}</span>
            </template>
            <el-descriptions :column="1" border size="default">
              <el-descriptions-item :label="$t('common.variantId')">
                <code>{{ detail.itemVariantId }}</code>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('item.itemId')">
                <code>{{ detail.itemId }}</code>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('common.registryName')">
                {{ detail.registryName }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('common.unlocalizedName')">
                {{ detail.unlocalizedName }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('common.mod')">
                {{ detail.modName }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('common.damageMeta')">
                {{ detail.damage
                }}<span v-if="detail.maxDamage > 0"> / {{ detail.maxDamage }}</span>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('item.maxStackSize')">
                {{ detail.maxStackSize }}
              </el-descriptions-item>
              <el-descriptions-item v-if="detail.nbtHash" :label="$t('common.nbtHash')">
                <code class="small">{{ detail.nbtHash }}</code>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="detail.tooltipText" shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('common.tooltip') }}</span>
            </template>
            <MinecraftTooltipText :text="detail.tooltipText" />
          </el-card>

          <el-card v-if="detail.nbtText" shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('common.nbt') }}</span>
            </template>
            <pre class="nbt-text">{{ detail.nbtText }}</pre>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="10">
          <el-card v-if="detail.chemicalExpression" shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('common.chemicalExpression') }}</span>
            </template>
            <code class="chemical-expression">{{ detail.chemicalExpression }}</code>
          </el-card>

          <el-card v-if="detail.worldGeneration.length" shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('item.worldGeneration') }}</span>
            </template>
            <div class="worldgen-list">
              <button
                v-for="resource in detail.worldGeneration"
                :key="`${resource.section}|${resource.key}`"
                type="button"
                class="worldgen-row"
                @click="goToWorldGeneration(resource.section, resource.key)"
              >
                <span class="worldgen-title">{{ resource.title }}</span>
                <span class="worldgen-type">{{ resource.type }}</span>
                <span v-if="resource.dimensions.length" class="worldgen-dimensions">
                  <span
                    v-for="dim in resource.dimensions.slice(0, 6)"
                    :key="dim.dimension"
                    class="worldgen-dimension-icon"
                  >
                    <img
                      v-if="dim.iconAssetUrl && resource.section !== 'bartworks-ores'"
                      :src="dim.iconAssetUrl"
                      :alt="dim.displayName"
                    />
                    <span v-else class="worldgen-dimension-text">{{ dim.displayName }}</span>
                  </span>
                </span>
                <span class="worldgen-stat">{{ resource.statText }}</span>
              </button>
            </div>
          </el-card>

          <el-card shadow="never" class="section">
            <template #header>
              <span class="section-title">{{ $t('common.extrasInfo') }}</span>
            </template>

            <el-skeleton v-if="extrasLoading" :rows="3" animated />
            <el-alert v-else-if="extrasError" :title="extrasError" type="error" :closable="false" />

            <template v-else-if="extras">
              <!-- 参与配方计数 -->
              <div class="counts">
                <button
                  type="button"
                  class="count-card"
                  :disabled="extras.asOutputRecipeCount === 0"
                  @click="goLookup('recipe')"
                >
                  <span class="count-label">{{ $t('common.asOutput') }}</span>
                  <span class="count-value">{{ extras.asOutputRecipeCount }}</span>
                  <span class="count-hint">{{ $t('common.recipeArrow') }}</span>
                </button>
                <button
                  type="button"
                  class="count-card"
                  :disabled="extras.asInputRecipeCount === 0"
                  @click="goLookup('usage')"
                >
                  <span class="count-label">{{ $t('common.asInput') }}</span>
                  <span class="count-value">{{ extras.asInputRecipeCount }}</span>
                  <span class="count-hint">{{ $t('common.usageArrow') }}</span>
                </button>
              </div>

              <!-- 矿典 -->
              <div v-if="extras.oreDictNames.length" class="ext-block">
                <div class="ext-block-title">
                  {{ $t('item.oreDictEntries') }}
                  <span class="ext-count">{{ extras.oreDictNames.length }}</span>
                </div>
                <div class="ore-tags">
                  <el-tag
                    v-for="name in extras.oreDictNames"
                    :key="name"
                    size="small"
                    type="success"
                    effect="plain"
                    round
                  >
                    {{ name }}
                  </el-tag>
                </div>
              </div>

              <!-- 流体容器 -->
              <div v-if="extras.fluidContainers.length" class="ext-block">
                <div class="ext-block-title">
                  {{ $t('common.fluidContainer') }}
                  <span class="ext-count">
                    {{
                      extras.fluidContainers.length === extras.fluidContainersTotal
                        ? extras.fluidContainersTotal
                        : `${extras.fluidContainers.length} / ${extras.fluidContainersTotal}`
                    }}
                  </span>
                </div>
                <div class="container-list">
                  <div
                    v-for="(c, idx) in extras.fluidContainers"
                    :key="`${c.fluidVariantId}-${c.containerItemVariantId}-${idx}`"
                    class="container-row"
                  >
                    <div
                      class="container-cell"
                      :title="c.containerDisplayName ?? c.containerItemVariantId"
                      @click="goToItem(c.containerItemVariantId)"
                    >
                      <img v-if="c.containerAssetUrl" :src="c.containerAssetUrl" loading="lazy" />
                      <span class="container-name">{{
                        c.containerDisplayName ?? c.containerItemVariantId
                      }}</span>
                    </div>
                    <div class="container-arrow">
                      <InteractiveFluidRef
                        :fluid="fluidRefFromContainer(c)"
                        variant="text"
                        @pick="pickFluid"
                        @lookup="lookupFluid"
                      />
                      <span v-if="c.amount > 0" class="container-amount">{{ c.amount }} mB</span>
                      <span class="container-arrow-line">→</span>
                    </div>
                    <div
                      class="container-cell"
                      :title="c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId"
                      @click="goToItem(c.emptyContainerItemVariantId)"
                    >
                      <img
                        v-if="c.emptyContainerAssetUrl"
                        :src="c.emptyContainerAssetUrl"
                        loading="lazy"
                      />
                      <span class="container-name">{{
                        c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId
                      }}</span>
                    </div>
                  </div>
                  <button
                    v-if="extras.fluidContainers.length < extras.fluidContainersTotal"
                    type="button"
                    class="see-all-btn"
                    @click="goToContainers"
                  >
                    {{ $t('item.viewAllContainers', { total: extras.fluidContainersTotal }) }}
                  </button>
                </div>
              </div>

              <!-- Thaumcraft 要素 -->
              <div v-if="extras.aspects.length" class="ext-block">
                <div class="ext-block-title">
                  {{ $t('item.thaumcraftAspects') }}
                  <span class="ext-count">{{ extras.aspects.length }}</span>
                </div>
                <div class="aspect-list">
                  <div
                    v-for="a in extras.aspects"
                    :key="a.aspectId"
                    class="aspect-item"
                    :title="a.description"
                  >
                    <img
                      v-if="a.iconAssetUrl"
                      :src="a.iconAssetUrl"
                      :alt="a.name"
                      class="aspect-icon"
                      loading="lazy"
                    />
                    <div class="aspect-meta">
                      <div class="aspect-name">
                        {{ a.name }}
                        <el-tag v-if="a.primal" size="small" type="warning" effect="plain" round>{{
                          $t('item.primal')
                        }}</el-tag>
                      </div>
                      <div class="aspect-amount">× {{ a.amount }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="!hasAnyExtras" class="ext-hint">{{ $t('common.noExtras') }}</div>
            </template>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" :description="$t('item.notFound')" />
  </div>
</template>

<style scoped>
.item-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.hero {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  background: var(--el-bg-color);
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}
.icon-wrap {
  width: 96px;
  height: 96px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.hero-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.title-block {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.title {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  word-break: break-word;
}
.subtitle {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.id {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  background: var(--el-fill-color-light);
  padding: 2px 6px;
  border-radius: 4px;
  cursor: pointer;
  user-select: all;
}
.id:hover {
  color: var(--el-color-primary);
}
.actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.section {
  margin-bottom: 16px;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
}
.nbt-text {
  background: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 12px;
  font-size: 12px;
  max-height: 360px;
  overflow: auto;
  margin: 0;
}
.preview-box {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 180px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
}
.preview-img {
  width: 140px;
  height: 140px;
  object-fit: contain;
  image-rendering: pixelated;
}
.preview-meta {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.ext-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.worldgen-list {
  display: grid;
  gap: 8px;
}
.worldgen-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  gap: 4px 8px;
  align-items: center;
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-light);
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}
.worldgen-row:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.worldgen-title {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
}
.worldgen-type,
.worldgen-dimensions,
.worldgen-stat {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}
.worldgen-type,
.worldgen-stat {
  text-align: right;
}
.worldgen-dimensions {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 5px;
  align-items: center;
  min-width: 0;
  overflow: hidden;
}
.worldgen-dimension-icon {
  min-width: 26px;
  width: auto;
  height: 26px;
  display: inline-grid;
  place-items: center;
}
.worldgen-dimension-icon img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.worldgen-dimension-text {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.worldgen-stat {
  color: var(--el-color-primary);
}

.counts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 12px;
}
.count-card {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
  text-align: left;
  font: inherit;
  color: inherit;
  transition:
    border-color 0.15s,
    background 0.15s;
}
.count-card:not(:disabled):hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.count-card:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
.count-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
}
.count-value {
  font-size: 18px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
.count-hint {
  font-size: 11px;
  color: var(--el-color-primary);
}

.ext-block {
  margin-top: 14px;
}
.ext-block-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular);
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}
.ext-count {
  font-weight: 400;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-variant-numeric: tabular-nums;
}
.ore-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.container-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.container-row {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}
.container-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 6px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  min-width: 0;
  background: var(--el-bg-color);
  transition: border-color 0.15s;
}
.container-cell:hover {
  border-color: var(--el-color-primary);
}
.container-cell img {
  width: 22px;
  height: 22px;
  object-fit: contain;
  image-rendering: pixelated;
  flex-shrink: 0;
}
.container-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}
.container-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 10px;
  line-height: 1.1;
  color: var(--el-text-color-secondary);
}
.container-fluid {
  color: var(--el-color-primary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.container-amount {
  font-variant-numeric: tabular-nums;
}
.container-arrow-line {
  font-size: 14px;
  color: var(--el-text-color-disabled);
}
.ext-more-hint {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}
.see-all-btn {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-color-primary);
  background: transparent;
  border: 1px dashed var(--el-color-primary-light-5);
  border-radius: 4px;
  padding: 6px 10px;
  cursor: pointer;
  width: 100%;
  text-align: center;
  transition:
    background 0.15s,
    border-color 0.15s;
}
.see-all-btn:hover {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}
.aspect-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.aspect-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
}
.aspect-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
}
.aspect-meta {
  display: flex;
  flex-direction: column;
}
.aspect-name {
  font-size: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}
.aspect-amount {
  font-size: 11px;
  color: var(--el-color-primary);
  font-variant-numeric: tabular-nums;
}

code {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  word-break: break-all;
}
code.small {
  color: var(--el-text-color-secondary);
}
.chemical-expression {
  display: block;
  font-family: 'WebNEI GTNH Glyphs', Consolas, 'Courier New', monospace;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.45;
  color: var(--el-color-primary);
}
</style>
