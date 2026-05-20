<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMobDetail } from '@/api/mobs'
import type { MobDetail, MobDropRow } from '@/api/mobs.types'

const route = useRoute()
const router = useRouter()

const datasetId = computed(() => String(route.params.datasetId ?? ''))
const mobVariantId = computed(() => String(route.params.mobVariantId ?? ''))

const detail = ref<MobDetail | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function load() {
  if (!datasetId.value || !mobVariantId.value) return
  loading.value = true
  error.value = null
  detail.value = null
  try {
    detail.value = await getMobDetail(datasetId.value, mobVariantId.value)
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

function goItem(itemVariantId: string) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: itemVariantId, kind: 'detail' },
  })
}

function back() {
  router.back()
}

// 掉落类型 → 中文 + 配色
const DROP_TYPE_LABEL: Record<string, { label: string; type: 'success' | 'info' | 'warning' }> = {
  normal: { label: '常规', type: 'success' },
  additional: { label: '附加', type: 'info' },
  rare: { label: '稀有', type: 'warning' },
}

function dropMeta(row: MobDropRow) {
  return DROP_TYPE_LABEL[row.dropType] ?? { label: row.dropType, type: 'info' as const }
}

function pct(p: number) {
  if (p >= 1) return '100%'
  if (p <= 0) return '0%'
  return `${(p * 100).toFixed(p >= 0.1 ? 1 : 2)}%`
}

watch([datasetId, mobVariantId], load)
onMounted(load)
</script>

<template>
  <div class="mob-detail">
    <header class="header">
      <el-button text @click="back">← 返回</el-button>
    </header>

    <el-skeleton v-if="loading" :rows="6" animated />
    <el-alert v-else-if="error" :title="error" type="error" :closable="false" show-icon />

    <template v-else-if="detail">
      <section class="hero">
        <div class="portrait">
          <img v-if="detail.summary.assetUrl" :src="detail.summary.assetUrl" :alt="detail.summary.displayName" />
        </div>
        <div class="hero-meta">
          <h1>{{ detail.summary.displayName }}</h1>
          <div class="entity-name">{{ detail.summary.entityName }}</div>
          <div class="tag-row">
            <el-tag size="small" effect="plain" round>{{ detail.summary.modId }}</el-tag>
            <el-tag size="small" type="danger" effect="plain" round>♥ {{ detail.summary.maxHealth }}</el-tag>
            <el-tag v-if="detail.summary.armor > 0" size="small" type="info" effect="plain" round>
              ⛨ {{ detail.summary.armor }}
            </el-tag>
            <el-tag size="small" type="info" effect="plain" round>
              {{ detail.summary.width.toFixed(2) }} × {{ detail.summary.height.toFixed(2) }} m
            </el-tag>
            <el-tag v-if="detail.summary.immuneToFire" size="small" type="warning" effect="plain" round>
              火免疫
            </el-tag>
            <el-tag v-if="detail.summary.leashable" size="small" type="success" effect="plain" round>
              可栓绳
            </el-tag>
          </div>
        </div>
      </section>

      <section class="info-grid">
        <div class="info-card">
          <div class="info-label">和平模式</div>
          <div class="info-value" :class="{ on: detail.info.allowedInPeaceful }">
            {{ detail.info.allowedInPeaceful ? '允许' : '消失' }}
          </div>
        </div>
        <div class="info-card">
          <div class="info-label">灵魂之瓶</div>
          <div class="info-value" :class="{ on: detail.info.soulVialUsable }">
            {{ detail.info.soulVialUsable ? '可装填' : '不可' }}
          </div>
        </div>
        <div class="info-card">
          <div class="info-label">精英怪</div>
          <div class="info-value" :class="{ on: detail.info.allowedInfernal }">
            <template v-if="detail.info.alwaysInfernal">必定</template>
            <template v-else-if="detail.info.allowedInfernal">可能</template>
            <template v-else>不会</template>
          </div>
        </div>
      </section>

      <section class="drops-section">
        <div class="section-title">
          掉落表 · {{ detail.drops.length }}
        </div>
        <div v-if="detail.drops.length === 0" class="empty-drops">
          <el-empty description="无掉落数据" :image-size="60" />
        </div>
        <div v-else class="drop-list">
          <div
            v-for="(d, idx) in detail.drops"
            :key="`${d.dropType}-${d.itemVariantId}-${idx}`"
            class="drop-row"
            role="button"
            tabindex="0"
            @click="goItem(d.itemVariantId)"
            @keydown.enter.prevent="goItem(d.itemVariantId)"
            @keydown.space.prevent="goItem(d.itemVariantId)"
          >
            <el-tag size="small" :type="dropMeta(d).type" effect="plain" round>
              {{ dropMeta(d).label }}
            </el-tag>
            <div class="drop-icon">
              <img v-if="d.assetUrl" :src="d.assetUrl" :alt="d.displayName ?? ''" loading="lazy" />
            </div>
            <div class="drop-name">
              {{ d.displayName ?? d.itemVariantId }}
            </div>
            <div class="drop-qty">× {{ d.stackSize }}</div>
            <div class="drop-prob">{{ pct(d.probability) }}</div>
            <div class="drop-tags">
              <el-tag v-if="d.playerOnly" size="small" type="warning" effect="plain" round>
                需玩家击杀
              </el-tag>
              <el-tag v-if="!d.lootable" size="small" type="info" effect="plain" round>
                非战利品袋
              </el-tag>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.mob-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header {
  display: flex;
  align-items: center;
}
.hero {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
}
.portrait {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px;
}
.portrait img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.hero-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.hero-meta h1 {
  margin: 0;
  font-size: 22px;
}
.entity-name {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px;
}
.info-card {
  padding: 10px 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}
.info-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}
.info-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-disabled);
}
.info-value.on {
  color: var(--el-color-primary);
}

.drops-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.section-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.empty-drops {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 12px;
}
.drop-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.drop-row {
  display: grid;
  grid-template-columns: 60px 36px 1fr auto 80px auto;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: border-color 0.15s, background 0.15s;
}
.drop-row:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.drop-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.drop-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.drop-name {
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.drop-qty {
  font-variant-numeric: tabular-nums;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.drop-prob {
  text-align: right;
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: var(--el-color-primary);
}
.drop-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
