<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useExtrasStore } from '@/stores/extras'
import { getFluidDetail } from '@/api/fluids'
import type { FluidDetail } from '@/api/fluids.types'
import type { FluidExtras } from '@/api/extras.types'

const props = defineProps<{
  datasetId: string
  fluidVariantId: string
}>()

const router = useRouter()
const extrasStore = useExtrasStore()

const detail = ref<FluidDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const heroImgFailed = ref(false)
const previewImgFailed = ref(false)

const extras = ref<FluidExtras | null>(null)
const extrasLoading = ref(false)
const extrasError = ref<string | null>(null)

const nbtLines = computed(() =>
  detail.value?.nbtText ? detail.value.nbtText.split('\n').filter(Boolean) : [],
)

const hasAnyExtras = computed(() => {
  if (!extras.value) return false
  return (
    extras.value.containers.length > 0
    || extras.value.blocks.length > 0
    || extras.value.asInputRecipeCount > 0
    || extras.value.asOutputRecipeCount > 0
  )
})

async function load() {
  if (!props.datasetId || !props.fluidVariantId) return
  loading.value = true
  error.value = null
  detail.value = null
  heroImgFailed.value = false
  previewImgFailed.value = false
  try {
    detail.value = await getFluidDetail(props.datasetId, props.fluidVariantId)
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function loadExtras() {
  if (!props.datasetId || !props.fluidVariantId) return
  extrasLoading.value = true
  extrasError.value = null
  extras.value = null
  try {
    extras.value = await extrasStore.loadFluid(props.datasetId, props.fluidVariantId)
  } catch (e) {
    extrasError.value = e instanceof Error ? e.message : String(e)
  } finally {
    extrasLoading.value = false
  }
}

function copyId() {
  if (!detail.value) return
  navigator.clipboard?.writeText(detail.value.fluidVariantId)
}

function goLookup(kind: 'recipe' | 'usage') {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: props.fluidVariantId, kind },
  })
}

function goToItem(itemVariantId: string) {
  router.replace({
    name: 'lookup',
    params: { datasetId: props.datasetId },
    query: { target: itemVariantId, kind: 'detail' },
  })
}

watch(() => [props.datasetId, props.fluidVariantId], () => {
  load()
  loadExtras()
})
onMounted(() => {
  load()
  loadExtras()
})
</script>

<template>
  <div class="fluid-detail">
    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading" :rows="6" animated />

    <div v-else-if="detail" class="content">
      <header class="hero">
        <div class="icon-wrap" :class="{ gaseous: detail.gaseous }">
          <img
            v-if="detail.assetUrl && !heroImgFailed"
            :src="detail.assetUrl"
            :alt="detail.displayName"
            class="hero-icon"
            @error="heroImgFailed = true"
          />
          <div v-else class="hero-fallback" :class="{ gaseous: detail.gaseous }">
            {{ detail.gaseous ? '气' : '液' }}
          </div>
        </div>
        <div class="title-block">
          <h1 class="title">{{ detail.displayName || detail.registryName }}</h1>
          <div class="subtitle">
            <el-tag size="default" type="info" effect="plain" round>{{ detail.modId }}</el-tag>
            <el-tag size="small" type="primary" effect="plain" round>流体</el-tag>
            <el-tag v-if="detail.gaseous" size="small" type="warning" effect="plain" round>气态</el-tag>
          </div>
          <div class="actions">
            <el-button type="primary" @click="goLookup('recipe')">查看合成 (R)</el-button>
            <el-button @click="goLookup('usage')">查看用途 (U)</el-button>
          </div>
        </div>
      </header>

      <el-row :gutter="16">
        <el-col :xs="24" :md="14">
          <el-card shadow="never" class="section">
            <template #header>
              <span class="section-title">基本属性</span>
            </template>
            <el-descriptions :column="1" border size="default">
              <el-descriptions-item label="变体 ID">
                <code>{{ detail.fluidVariantId }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="流体 ID">
                <code>{{ detail.fluidId }}</code>
              </el-descriptions-item>
              <el-descriptions-item label="注册名">
                {{ detail.registryName }}
              </el-descriptions-item>
              <el-descriptions-item label="非本地化名">
                {{ detail.unlocalizedName }}
              </el-descriptions-item>
              <el-descriptions-item label="Mod">
                {{ detail.modId }}
              </el-descriptions-item>
              <el-descriptions-item label="形态">
                {{ detail.gaseous ? '气态' : '液态' }}
              </el-descriptions-item>
              <el-descriptions-item label="温度">
                {{ detail.temperature.toLocaleString() }} K
              </el-descriptions-item>
              <el-descriptions-item label="密度">
                {{ detail.density.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="粘度">
                {{ detail.viscosity.toLocaleString() }}
              </el-descriptions-item>
              <el-descriptions-item label="发光">
                {{ detail.luminosity }}
              </el-descriptions-item>
              <el-descriptions-item v-if="detail.nbtHash" label="NBT Hash">
                <code class="small">{{ detail.nbtHash }}</code>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card v-if="nbtLines.length" shadow="never" class="section">
            <template #header>
              <span class="section-title">NBT</span>
            </template>
            <pre class="nbt-text">{{ detail.nbtText }}</pre>
          </el-card>
        </el-col>

        <el-col :xs="24" :md="10">
          <el-card shadow="never" class="section">
            <template #header>
              <span class="section-title">渲染预览</span>
            </template>
            <div class="preview-box" :class="{ gaseous: detail.gaseous }">
              <img
                v-if="detail.assetUrl && !previewImgFailed"
                :src="detail.assetUrl"
                :alt="detail.displayName"
                class="preview-img"
                @error="previewImgFailed = true"
              />
              <div v-else class="preview-fallback" :class="{ gaseous: detail.gaseous }">
                {{ detail.gaseous ? '气' : '液' }}
              </div>
            </div>
          </el-card>

          <el-card shadow="never" class="section">
            <template #header>
              <span class="section-title">扩展信息</span>
            </template>

            <el-skeleton v-if="extrasLoading" :rows="3" animated />
            <el-alert v-else-if="extrasError" :title="extrasError" type="error" :closable="false" />

            <template v-else-if="extras">
              <div class="counts">
                <button
                  type="button"
                  class="count-card"
                  :disabled="extras.asOutputRecipeCount === 0"
                  @click="goLookup('recipe')"
                >
                  <span class="count-label">作为输出</span>
                  <span class="count-value">{{ extras.asOutputRecipeCount }}</span>
                  <span class="count-hint">合成 →</span>
                </button>
                <button
                  type="button"
                  class="count-card"
                  :disabled="extras.asInputRecipeCount === 0"
                  @click="goLookup('usage')"
                >
                  <span class="count-label">作为输入</span>
                  <span class="count-value">{{ extras.asInputRecipeCount }}</span>
                  <span class="count-hint">用途 →</span>
                </button>
              </div>

              <div v-if="extras.blocks.length" class="ext-block">
                <div class="ext-block-title">
                  对应方块
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
                    <img v-if="b.blockAssetUrl" :src="b.blockAssetUrl" loading="lazy" />
                    <span class="block-name">{{ b.blockDisplayName ?? b.blockItemVariantId }}</span>
                  </div>
                </div>
              </div>

              <div v-if="extras.containers.length" class="ext-block">
                <div class="ext-block-title">
                  流体容器
                  <span class="ext-count">{{ extras.containers.length }}</span>
                </div>
                <div class="container-list">
                  <div
                    v-for="(c, idx) in extras.containers"
                    :key="`${c.fluidVariantId}-${c.containerItemVariantId}-${idx}`"
                    class="container-row"
                  >
                    <div class="container-cell" :title="c.containerDisplayName ?? c.containerItemVariantId" @click="goToItem(c.containerItemVariantId)">
                      <img v-if="c.containerAssetUrl" :src="c.containerAssetUrl" loading="lazy" />
                      <span class="container-name">{{ c.containerDisplayName ?? c.containerItemVariantId }}</span>
                    </div>
                    <div class="container-arrow">
                      <span v-if="c.amount > 0" class="container-amount">{{ c.amount }} mB</span>
                      <span class="container-arrow-line">→</span>
                    </div>
                    <div class="container-cell" :title="c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId" @click="goToItem(c.emptyContainerItemVariantId)">
                      <img v-if="c.emptyContainerAssetUrl" :src="c.emptyContainerAssetUrl" loading="lazy" />
                      <span class="container-name">{{ c.emptyContainerDisplayName ?? c.emptyContainerItemVariantId }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="!hasAnyExtras" class="ext-hint">无扩展信息</div>
            </template>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-empty v-else-if="!loading" description="未找到流体" />
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
  transition: border-color 0.15s, background 0.15s;
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
</style>
