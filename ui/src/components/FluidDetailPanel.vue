<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useExtrasStore } from '@/stores/extras';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import { getFluidDetail } from '@/api/fluids';
import type { FluidDetail } from '@/api/fluids.types';
import type { FluidExtras } from '@/api/extras.types';
import DetailTextCard from './entity-detail/DetailTextCard.vue';
import EntityDetailLayout from './entity-detail/EntityDetailLayout.vue';
import FluidAttributesCard from './entity-detail/FluidAttributesCard.vue';
import FluidBlocksBlock from './entity-detail/FluidBlocksBlock.vue';
import FluidContainersBlock from './entity-detail/FluidContainersBlock.vue';
import FluidUndergroundResourcesCard from './entity-detail/FluidUndergroundResourcesCard.vue';

const props = defineProps<{
  datasetId: string;
  fluidVariantId: string;
}>();

const router = useRouter();
const entityNavigation = useEntityNavigation(
  router,
  computed(() => props.datasetId),
);
const extrasStore = useExtrasStore();
const { t } = useI18n();

const detail = ref<FluidDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const extras = ref<FluidExtras | null>(null);

const gaseousLabel = computed(() => t(detail.value?.gaseous ? 'fluid.gaseous' : 'fluid.liquid'));

async function load() {
  if (!props.datasetId || !props.fluidVariantId) return;
  loading.value = true;
  error.value = null;
  detail.value = null;
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
  extras.value = null;
  try {
    extras.value = await extrasStore.loadFluid(props.datasetId, props.fluidVariantId);
  } catch {
    extras.value = null;
  }
}

function goToItem(itemVariantId: string) {
  entityNavigation.pick(itemVariantId);
}

function lookupItem(kind: 'recipe' | 'usage', itemVariantId: string) {
  entityNavigation.lookup(kind, itemVariantId);
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
  <EntityDetailLayout
    :loading="loading"
    :error="error"
    :has-content="!!detail"
    :not-found-text="$t('fluid.notFound')"
    :skeleton-rows="6"
  >
    <template v-if="detail">
      <el-row :gutter="16">
        <el-col
          :xs="24"
          :md="14"
        >
          <FluidAttributesCard
            :detail="detail"
            :state-label="gaseousLabel"
          />

          <DetailTextCard
            v-if="detail.nbtText"
            :title="$t('common.nbt')"
            :text="detail.nbtText"
            pre
          />
        </el-col>

        <el-col
          :xs="24"
          :md="10"
        >
          <template v-if="extras">
            <FluidBlocksBlock
              v-if="extras.blocks.length"
              :blocks="extras.blocks"
              @open-item="goToItem"
              @lookup-item="lookupItem"
            />
          </template>

          <DetailTextCard
            v-if="detail.chemicalExpression"
            :title="$t('common.chemicalExpression')"
            :text="detail.chemicalExpression"
            code
          />

          <FluidUndergroundResourcesCard
            v-if="detail.undergroundResources.length"
            :resources="detail.undergroundResources"
            @open="goToUndergroundResource"
          />

          <template v-if="extras">
            <FluidContainersBlock
              v-if="extras.containers.length"
              :containers="extras.containers"
              @open-item="goToItem"
            />
          </template>
        </el-col>
      </el-row>
    </template>
  </EntityDetailLayout>
</template>
