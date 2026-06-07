<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useExtrasStore } from '@/stores/extras';
import { getFluidDetail } from '@/api/fluids';
import type { FluidDetail } from '@/api/fluids.types';
import type { FluidExtras } from '@/api/extras.types';
import { formatChancePercent } from '@/utils/format';

const props = defineProps<{
  datasetId: string;
  fluidVariantId: string;
}>();

const router = useRouter();
const extrasStore = useExtrasStore();
const { t } = useI18n();

const detail = ref<FluidDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);
const heroImgFailed = ref(false);

const extras = ref<FluidExtras | null>(null);
const extrasLoading = ref(false);
const extrasError = ref<string | null>(null);

const nbtLines = computed(() =>
  detail.value?.nbtText ? detail.value.nbtText.split('\n').filter(Boolean) : [],
);

const hasAnyExtras = computed(() => {
  if (!extras.value) return false;
  return (
    extras.value.containers.length > 0 ||
    extras.value.blocks.length > 0 ||
    extras.value.asInputRecipeCount > 0 ||
    extras.value.asOutputRecipeCount > 0
  );
});

const gaseousLabel = computed(() => t(detail.value?.gaseous ? 'fluid.gaseous' : 'fluid.liquid'));

const gaseousShortLabel = computed(() =>
  detail.value?.gaseous ? t('fluid.gaseous').charAt(0) : t('fluid.liquid').charAt(0),
);

async function load() {
  if (!props.datasetId || !props.fluidVariantId) return;
  loading.value = true;
  error.value = null;
  detail.value = null;
  heroImgFailed.value = false;
  try {
    detail.value = await getFluidDetail(props.datasetId, props.fluidVariantId);
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

async function loadExtras() {
  if (!props.datasetId || !props.fluidVariantId) return;
  extrasLoading.value = true;
  extrasError.value = null;
  extras.value = null;
  try {
    extras.value = await extrasStore.loadFluid(props.datasetId, props.fluidVariantId);
  } catch (e) {
    extrasError.value = e instanceof Error ? e.message : String(e);
  } finally {
    extrasLoading.value = false;
  }
}

function goLookup(kind: 'recipe' | 'usage') {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: props.fluidVariantId, kind },
  });
}

function goToItem(itemVariantId: string) {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: itemVariantId, kind: 'detail' },
  });
}

function goToUndergroundResource(fluidId: string, dimension: string) {
  router.push({
    name: 'gt',
    params: { datasetId: props.datasetId, section: 'underground-fluids' },
    query: { fluidId, dimension },
  });
}

watch(
  () => [props.datasetId, props.fluidVariantId],
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
  <div class="fluid-detail">
    <el-alert
      v-if="error"
      :title="error"
      type="error"
      :closable="false"
      show-icon
    />
    <el-skeleton
      v-if="loading"
      :rows="6"
      animated
    />

    <div
      v-else-if="detail"
      class="content"
    >
      <header class="hero">
        <div
          class="icon-wrap"
          :class="{ gaseous: detail.gaseous }"
        >
          <img
            v-if="detail.assetUrl && !heroImgFailed"
            :src="detail.assetUrl"
            :alt="detail.displayName"
            class="hero-icon"
            @error="heroImgFailed = true"
          >
          <div
            v-else
            class="hero-fallback"
            :class="{ gaseous: detail.gaseous }"
          >
            {{ gaseousShortLabel }}
          </div>
        </div>
        <div class="title-block">
          <h1 class="title">
            {{ detail.displayName || detail.registryName }}
          </h1>
          <div class="subtitle">
            <el-tag
              size="default"
              type="info"
              effect="plain"
              round
            >
              {{ detail.modName }}
            </el-tag>
            <el-tag
              size="small"
              type="primary"
              effect="plain"
              round
            >
              {{ $t('fluid.tag') }}
            </el-tag>
            <el-tag
              v-if="detail.gaseous"
              size="small"
              type="warning"
              effect="plain"
              round
            >
              {{
                $t('fluid.gaseous')
              }}
            </el-tag>
          </div>
          <div class="actions">
            <el-button
              type="primary"
              @click="goLookup('recipe')"
            >
              {{
                $t('common.viewRecipe')
              }}
            </el-button>
            <el-button @click="goLookup('usage')">
              {{ $t('common.viewUsage') }}
            </el-button>
          </div>
        </div>
      </header>

      <el-row :gutter="16">
        <el-col
          :xs="24"
          :md="14"
        >
          <el-card
            shadow="never"
            class="section"
          >
            <template #header>
              <span class="section-title">{{ $t('common.basicAttributes') }}</span>
            </template>
            <el-descriptions
              :column="1"
              border
              size="default"
            >
              <el-descriptions-item :label="$t('common.variantId')">
                <code>{{ detail.fluidVariantId }}</code>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('fluid.fluidId')">
                <code>{{ detail.fluidId }}</code>
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
              <el-descriptions-item :label="$t('fluid.state')">
                {{ gaseousLabel }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('fluid.temperature')">
                {{ detail.temperature.toLocaleString() }} K
              </el-descriptions-item>
              <el-descriptions-item :label="$t('fluid.density')">
                {{ detail.density.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('fluid.viscosity')">
                {{ detail.viscosity.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('fluid.luminosity')">
                {{ detail.luminosity }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="detail.nbtHash"
                :label="$t('common.nbtHash')"
              >
                <code class="small">{{ detail.nbtHash }}</code>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card
            v-if="nbtLines.length"
            shadow="never"
            class="section"
          >
            <template #header>
              <span class="section-title">{{ $t('common.nbt') }}</span>
            </template>
            <pre class="nbt-text">{{ detail.nbtText }}</pre>
          </el-card>
        </el-col>

        <el-col
          :xs="24"
          :md="10"
        >
          <el-card
            v-if="detail.chemicalExpression"
            shadow="never"
            class="section"
          >
            <template #header>
              <span class="section-title">{{ $t('common.chemicalExpression') }}</span>
            </template>
            <code class="chemical-expression">{{ detail.chemicalExpression }}</code>
          </el-card>

          <el-card
            v-if="detail.undergroundResources.length"
            shadow="never"
            class="section"
          >
            <template #header>
              <span class="section-title">{{ $t('fluid.undergroundResources') }}</span>
            </template>
            <div class="underground-list">
              <button
                v-for="resource in detail.undergroundResources"
                :key="`${resource.fluidId}|${resource.dimension}`"
                type="button"
                class="underground-row"
                @click="goToUndergroundResource(resource.fluidId, resource.dimension)"
              >
                <span class="underground-dimension">
                  <img
                    v-if="resource.dimensionDisplay.iconAssetUrl"
                    :src="resource.dimensionDisplay.iconAssetUrl"
                    :alt="resource.dimensionDisplay.displayName"
                  >
                  <span class="underground-dimension-name">
                    {{ resource.dimensionDisplay.displayName }}
                  </span>
                </span>
                <span class="underground-stats">
                  <el-tag
                    size="small"
                    effect="plain"
                    round
                  >
                    {{ resource.minAmount }}-{{ resource.maxAmount }} L
                  </el-tag>
                  <el-tag
                    size="small"
                    type="warning"
                    effect="plain"
                    round
                  >
                    {{ $t('common.probability') }} {{ formatChancePercent(resource.chance) }}
                  </el-tag>
                </span>
              </button>
            </div>
          </el-card>

          <el-card
            shadow="never"
            class="section"
          >
            <template #header>
              <span class="section-title">{{ $t('common.extrasInfo') }}</span>
            </template>

            <el-skeleton
              v-if="extrasLoading"
              :rows="3"
              animated
            />
            <el-alert
              v-else-if="extrasError"
              :title="extrasError"
              type="error"
              :closable="false"
            />

            <template v-else-if="extras">
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

              <div
                v-if="extras.blocks.length"
                class="ext-block"
              >
                <div class="ext-block-title">
                  {{ $t('fluid.correspondingBlock') }}
                  <span class="ext-count">{{ extras.blocks.length }}</span>
                </div>
                <div class="block-list">
                  <div
                    v-for="b in extras.blocks"
                    :key="b.blockItemVariantId"
                    class="block-cell"
                    :title="b.blockDisplayName ?? b.blockItemVariantId"
                    @click="goToItem(b.blockItemVariantId)"
                  >
                    <img
                      v-if="b.blockAssetUrl"
                      :src="b.blockAssetUrl"
                      loading="lazy"
                    >
                    <span class="block-name">{{ b.blockDisplayName ?? b.blockItemVariantId }}</span>
                  </div>
                </div>
              </div>

              <div
                v-if="extras.containers.length"
                class="ext-block"
              >
                <div class="ext-block-title">
                  {{ $t('common.fluidContainer') }}
                  <span class="ext-count">{{ extras.containers.length }}</span>
                </div>
                <div class="container-list">
                  <div
                    v-for="(c, idx) in extras.containers"
                    :key="`${c.fluidVariantId}-${c.containerItemVariantId}-${idx}`"
                    class="container-row"
                  >
                    <div
                      class="container-cell"
                      :title="c.containerDisplayName ?? c.containerItemVariantId"
                      @click="goToItem(c.containerItemVariantId)"
                    >
                      <img
                        v-if="c.containerAssetUrl"
                        :src="c.containerAssetUrl"
                        loading="lazy"
                      >
                      <span class="container-name">{{
                        c.containerDisplayName ?? c.containerItemVariantId
                      }}</span>
                    </div>
                    <div class="container-arrow">
                      <span
                        v-if="c.amount > 0"
                        class="container-amount"
                      >{{ c.amount }} mB</span>
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
                      >
                      <span class="container-name">{{
                        c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId
                      }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div
                v-if="!hasAnyExtras"
                class="ext-hint"
              >
                {{ $t('common.noExtras') }}
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty
      v-else-if="!loading"
      :description="$t('fluid.notFound')"
    />
  </div>
</template>

<style scoped>
.fluid-detail {
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
  background: rgba(33, 130, 230, 0.12);
  border: 1px solid rgba(33, 130, 230, 0.4);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.icon-wrap.gaseous {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.5);
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
  background: rgba(33, 130, 230, 0.1);
  border-radius: 6px;
}
.preview-box.gaseous {
  background: rgba(245, 158, 11, 0.08);
}
.preview-img {
  width: 140px;
  height: 140px;
  object-fit: contain;
  image-rendering: pixelated;
}
.hero-fallback {
  font-size: 36px;
  font-weight: 700;
  color: rgba(33, 130, 230, 0.9);
}
.hero-fallback.gaseous {
  color: rgba(245, 158, 11, 0.95);
}
.preview-fallback {
  font-size: 64px;
  font-weight: 700;
  color: rgba(33, 130, 230, 0.85);
}
.preview-fallback.gaseous {
  color: rgba(245, 158, 11, 0.9);
}
.ext-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
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

.underground-list {
  display: grid;
  gap: 8px;
}
.underground-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) max-content;
  gap: 8px;
  align-items: center;
  width: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-light);
  padding: 8px 10px;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}
.underground-row:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.underground-dimension {
  min-width: 0;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  align-items: center;
  gap: 8px;
}
.underground-dimension img {
  width: 16px;
  height: 16px;
  object-fit: contain;
  image-rendering: pixelated;
  image-rendering: crisp-edges;
}
.underground-dimension-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.underground-stats {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  gap: 4px;
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
.block-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.block-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  cursor: pointer;
  background: var(--el-bg-color);
  transition: border-color 0.15s;
  font-size: 12px;
}
.block-cell:hover {
  border-color: var(--el-color-primary);
}
.block-cell img {
  width: 24px;
  height: 24px;
  object-fit: contain;
  image-rendering: pixelated;
}
.block-name {
  font-size: 12px;
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
