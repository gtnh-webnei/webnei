<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { listQuestLines } from '@/api/quests';
import QuestText from '@/components/QuestText.vue';
import type { QuestLineSummary } from '@/api/quests.types';

const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);

const { t } = useI18n();

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));

const lines = ref<QuestLineSummary[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const q = ref('');

const filtered = computed(() => {
  const needle = q.value.trim().toLowerCase();
  if (!needle) return lines.value;
  return lines.value.filter(
    (l) => l.name.toLowerCase().includes(needle) || l.description.toLowerCase().includes(needle),
  );
});

async function load() {
  if (!datasetId.value) return;
  loading.value = true;
  error.value = null;
  try {
    lines.value = await listQuestLines(datasetId.value);
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

function openLine(line: QuestLineSummary) {
  router.push({
    name: 'quest-line',
    params: { datasetId: datasetId.value },
    query: { id: line.questLineId },
  });
}

watch(datasetId, load);
onMounted(load);
</script>

<template>
  <div class="quest-lines">
    <header class="header">
      <h1>{{ $t('quest.pageTitle') }}</h1>
      <p class="lead">
        {{ $t('common.totalCount') }} {{ lines.length }}{{ $t('quest.questLineSummary') }}
      </p>
    </header>

    <div class="toolbar">
      <el-input
        v-model="q"
        :placeholder="$t('quest.searchPlaceholder')"
        clearable
        class="search-input"
      />
      <div class="spacer" />
      <div class="total">
        {{ $t('common.showing') }}<strong>{{ filtered.length }}</strong> / {{ lines.length }}
      </div>
    </div>

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading && lines.length === 0" :rows="6" animated />
    <el-empty v-else-if="filtered.length === 0" :description="$t('quest.noQuestLines')" />

    <div v-else class="grid">
      <div
        v-for="line in filtered"
        :key="line.questLineId"
        class="line-card"
        role="button"
        tabindex="0"
        @click="openLine(line)"
        @keydown.enter.prevent="openLine(line)"
        @keydown.space.prevent="openLine(line)"
      >
        <div class="icon-wrap">
          <img v-if="line.iconAssetUrl" :src="line.iconAssetUrl" :alt="line.name" loading="lazy" />
        </div>
        <div class="meta">
          <div class="name">{{ line.name }}</div>
          <div class="sub">
            <el-tag size="small" type="info" effect="plain" round>{{
              $t('quest.taskCount', { count: line.questCount })
            }}</el-tag>
          </div>
          <QuestText v-if="line.description" :text="line.description" class="desc" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quest-lines {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.header h1 {
  margin: 0 0 4px 0;
  font-size: 22px;
}
.lead {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}
.line-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  font: inherit;
  color: inherit;
  text-align: left;
  transition:
    border-color 0.15s,
    background 0.15s,
    box-shadow 0.15s;
}
.line-card:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.icon-wrap {
  width: 48px;
  height: 48px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
}
.icon-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.3;
}
.sub {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
