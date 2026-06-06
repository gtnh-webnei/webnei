<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import { useDatasetStore } from '@/stores/dataset';

const { t } = useI18n();
const store = useDatasetStore();
const { datasets, loading, error } = storeToRefs(store);
</script>

<template>
  <div class="dataset-index">
    <header class="page-header">
      <h1>{{ t('dataset.brandName') }}</h1>
      <p class="lead">{{ t('dataset.leadText') }}</p>
    </header>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading" :rows="4" animated />
    <el-empty v-else-if="datasets.length === 0" :description="t('dataset.emptyState')" />

    <el-row v-else :gutter="20">
      <el-col v-for="d in datasets" :key="d.datasetId" :xs="24" :sm="24" :md="12" :lg="12" :xl="8">
        <el-card class="dataset-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="title-block">
                <span class="title">{{ d.displayName }}</span>
                <span class="subtitle">
                  {{ d.packSlug }} · {{ d.packVersion }} · {{ d.variant }} · {{ d.language }}
                </span>
              </div>
              <el-tag type="info" effect="plain" size="small"> MC {{ d.minecraftVersion }} </el-tag>
            </div>
          </template>

          <div class="meta">
            <div class="meta-row">
              <span class="k">{{ t('dataset.exporter') }}</span>
              <span>{{ d.exporterVersion }} · schema v{{ d.schemaVersion }}</span>
            </div>
            <div class="meta-row">
              <span class="k">{{ t('dataset.exportTime') }}</span>
              <span>{{ new Date(d.createdAt).toLocaleString() }}</span>
            </div>
            <div class="meta-row plugins">
              <span class="k">{{ t('dataset.plugins') }}</span>
              <span class="tags">
                <el-tag v-for="p in d.activePlugins" :key="p" size="small" effect="light" round>
                  {{ p }}
                </el-tag>
              </span>
            </div>
          </div>

          <template #footer>
            <div class="actions">
              <router-link :to="`/datasets/${encodeURIComponent(d.datasetId)}/items`">
                <el-button type="primary">{{ t('dataset.startBrowsing') }}</el-button>
              </router-link>
              <router-link :to="`/datasets/${encodeURIComponent(d.datasetId)}/mods`">
                <el-button>{{ t('dataset.modList') }}</el-button>
              </router-link>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 24px;
}
.page-header h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
}
.lead {
  color: var(--el-text-color-secondary);
  margin: 0;
}
.dataset-card {
  margin-bottom: 20px;
}
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.title-block {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.title {
  font-weight: 600;
  font-size: 16px;
}
.subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}
.meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.meta-row {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--el-text-color-regular);
  align-items: baseline;
}
.meta-row .k {
  width: 84px;
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.meta-row.plugins {
  align-items: flex-start;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.actions {
  display: flex;
  gap: 8px;
}
</style>
