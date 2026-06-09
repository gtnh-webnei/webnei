<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { listMobMods, listMobs } from '@/api/mobs';
import type { MobSummary } from '@/api/mobs.types';
import { usePagedBrowser } from '@/composables/usePagedBrowser';
import BrowserToolbar from '@/components/BrowserToolbar.vue';
import MobCard from '@/components/MobCard.vue';

const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));

const browser = usePagedBrowser<MobSummary>({
  datasetId,
  storageKey: 'webnei.mobBrowser.size',
  defaultSize: 48,
  fetcher: (id, { q, secondary, page, size }) => listMobs(id, { q, modId: secondary, page, size }),
  optionsFetcher: listMobMods,
});
const { q, secondary, page, pageSize, items, total, loading, error, secondaryOptions } = browser;

function openMob(mob: MobSummary) {
  router.push({
    name: 'mob',
    params: { datasetId: datasetId.value, mobVariantId: mob.mobVariantId },
  });
}
</script>

<template>
  <div class="mob-browser">
    <BrowserToolbar
      v-model:q="q"
      v-model:secondary="secondary"
      :secondary-options="secondaryOptions"
      :placeholder="$t('mob.searchPlaceholder')"
      :secondary-placeholder="$t('common.allMod')"
      :total="total"
    />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading && items.length === 0" :rows="6" animated />
    <el-empty v-else-if="!loading && items.length === 0" :description="$t('mob.noMatch')" />

    <div v-else v-loading="loading" class="grid">
      <MobCard v-for="mob in items" :key="mob.mobVariantId" :mob="mob" @select="openMob" />
    </div>

    <div v-if="total > 0" class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[24, 48, 96, 192]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.mob-browser {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
