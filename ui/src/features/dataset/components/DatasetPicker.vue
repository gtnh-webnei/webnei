<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import type { DatasetListEntry } from '@features/dataset/types'

const emit = defineEmits<{ (event: 'select'): void }>()

const { t, te } = useI18n()
const store = useDatasetStore()
const { datasets, activeDataset, loading, error } = storeToRefs(store)

interface PackOption {
  key: string
  title: string
  datasets: DatasetListEntry[]
}

// 按整合包分组（slug+version+variant），语言从属于包。
const packs = computed<PackOption[]>(() => {
  const map = new Map<string, PackOption>()
  for (const dataset of datasets.value) {
    const key = `${dataset.packSlug}:${dataset.packVersion}:${dataset.variant}`
    const pack = map.get(key)
    if (pack) {
      pack.datasets.push(dataset)
    } else {
      map.set(key, {
        key,
        title: `${dataset.packSlug} · ${dataset.packVersion} · ${dataset.variant}`,
        datasets: [dataset],
      })
    }
  }
  return [...map.values()]
})

function packKeyOf(dataset: DatasetListEntry): string {
  return `${dataset.packSlug}:${dataset.packVersion}:${dataset.variant}`
}

const activePackKey = computed(() => (activeDataset.value ? packKeyOf(activeDataset.value) : null))

const activePack = computed(() => packs.value.find((pack) => pack.key === activePackKey.value) ?? null)

const languages = computed(() => activePack.value?.datasets ?? [])

function languageLabel(language: string): string {
  const key = `language.${language}`
  return te(key) ? t(key) : language
}

// 切包：保持当前语言，该包无此语言则取该包第一个。
function onPackChange(packKey: string) {
  const pack = packs.value.find((entry) => entry.key === packKey)
  if (!pack) return
  const current = activeDataset.value?.language
  const target = pack.datasets.find((dataset) => dataset.language === current) ?? pack.datasets[0]
  if (!target) return
  store.selectDataset(target.datasetId)
  emit('select')
}

function onLanguageChange(datasetId: string) {
  store.selectDataset(datasetId)
  emit('select')
}
</script>

<template>
  <div class="dataset-picker">
    <p class="picker-title">
      {{ t('dataset.title') }}
    </p>

    <p
      v-if="loading"
      class="picker-hint"
    >
      {{ t('dataset.loading') }}
    </p>
    <p
      v-else-if="error"
      class="picker-hint is-error"
    >
      {{ t('dataset.loadError') }}
    </p>
    <p
      v-else-if="packs.length === 0"
      class="picker-hint"
    >
      {{ t('dataset.empty') }}
    </p>

    <template v-else>
      <label class="field">
        <span class="field-label">{{ t('dataset.pack') }}</span>
        <el-select
          :model-value="activePackKey ?? undefined"
          :placeholder="t('dataset.pack')"
          @change="onPackChange"
        >
          <el-option
            v-for="pack in packs"
            :key="pack.key"
            :label="pack.title"
            :value="pack.key"
          />
        </el-select>
      </label>

      <label class="field">
        <span class="field-label">{{ t('dataset.language') }}</span>
        <el-select
          :model-value="activeDataset?.datasetId ?? undefined"
          :placeholder="t('dataset.language')"
          @change="onLanguageChange"
        >
          <el-option
            v-for="dataset in languages"
            :key="dataset.datasetId"
            :label="languageLabel(dataset.language)"
            :value="dataset.datasetId"
          />
        </el-select>
      </label>
    </template>
  </div>
</template>

<style scoped>
.dataset-picker {
  display: grid;
  gap: 12px;
  width: 100%;
}

.picker-title {
  margin: 0;
  color: var(--mc-text-muted);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.picker-hint {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.picker-hint.is-error {
  color: var(--mc-redstone);
}

.field {
  display: grid;
  gap: 4px;
}

.field-label {
  color: var(--mc-text-muted);
  font-size: 12px;
  font-weight: 700;
}
</style>
