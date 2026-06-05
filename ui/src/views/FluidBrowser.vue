<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useDatasetStore } from '@/stores/dataset';
import { listFluidMods, listFluids } from '@/api/fluids';
import type { FluidSummary } from '@/api/fluids.types';
import { usePagedBrowser } from '@/composables/usePagedBrowser';
import BrowserToolbar from '@/components/BrowserToolbar.vue';
import FluidCard from '@/components/FluidCard.vue';

const { t } = useI18n();

const route = useRoute();
const router = useRouter();
const datasetStore = useDatasetStore();
const { activeDatasetId } = storeToRefs(datasetStore);

const datasetId = computed(() => String(route.params.datasetId ?? activeDatasetId.value ?? ''));

const browser = usePagedBrowser<FluidSummary>({
  datasetId,
  storageKey: 'webnei.fluidBrowser.size',
  defaultSize: 60,
  fetcher: (id, { q, secondary, page, size }) =>
    listFluids(id, { q, modId: secondary, page, size }),
  optionsFetcher: listFluidMods,
});
const { q, secondary, page, pageSize, items, total, loading, error, secondaryOptions } = browser;

function openDetail(fluid: FluidSummary) {
  router.push({
    name: 'lookup',
    params: { datasetId: datasetId.value },
    query: { target: fluid.fluidVariantId, kind: 'detail' },
  });
}
</script>

<template>
  <div class="fluid-browser">
    <BrowserToolbar
      v-model:q="q"
      v-model:secondary="secondary"
      :secondary-options="secondaryOptions"
      :placeholder="t('fluid.searchPlaceholder')"
      :total="total"
    />

    <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon />
    <el-skeleton v-if="loading && items.length === 0" :rows="6" animated />
    <el-empty v-else-if="items.length === 0" :description="t('fluid.noMatch')" />

    <div v-else v-loading="loading" class="grid">
      <FluidCard v-for="f in items" :key="f.fluidVariantId" :fluid="f" @select="openDetail" />
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[30, 60, 120, 240]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.fluid-browser {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 100%;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 10px;
  width: 100%;
}
.pager {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}
</style>
