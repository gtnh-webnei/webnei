<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import { getFluidDetail } from '@/api/fluids';
import type { FluidDetail } from '@/api/fluids.types';
import DetailTextCard from './entity-detail/DetailTextCard.vue';
import EntityDetailLayout from './entity-detail/EntityDetailLayout.vue';
import FluidAttributesCard from './entity-detail/FluidAttributesCard.vue';
import FluidBlocksBlock from './entity-detail/FluidBlocksBlock.vue';
import FluidContainersBlock from './entity-detail/FluidContainersBlock.vue';
import FluidUndergroundResourcesCard from './entity-detail/FluidUndergroundResourcesCard.vue';
import LookupRecipeCountCard from './recipe/LookupRecipeCountCard.vue';

const props = defineProps<{
  datasetId: string;
  fluidVariantId: string;
  recipeCount?: number;
  usageCount?: number;
}>();

const emit = defineEmits<{
  'select-lookup-kind': [kind: 'recipe' | 'usage'];
}>();

const router = useRouter();
const entityNavigation = useEntityNavigation(
  router,
  computed(() => props.datasetId),
);
const { t } = useI18n();

const detail = ref<FluidDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

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
  },
);
onMounted(() => {
  load();
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
        <el-col :xs="24" :md="14">
          <FluidAttributesCard :detail="detail" :state-label="gaseousLabel" />

          <DetailTextCard
            v-if="detail.nbtText"
            :title="$t('common.nbt')"
            :text="detail.nbtText"
            pre
          />
        </el-col>

        <el-col :xs="24" :md="10">
          <LookupRecipeCountCard
            v-if="props.recipeCount !== undefined && props.usageCount !== undefined"
            :recipe-count="props.recipeCount"
            :usage-count="props.usageCount"
            :active-kind="null"
            @select-kind="emit('select-lookup-kind', $event)"
          />

          <FluidBlocksBlock
            v-if="detail.blocks.length"
            :blocks="detail.blocks"
            @open-item="goToItem"
            @lookup-item="lookupItem"
          />

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

          <FluidContainersBlock
            v-if="detail.containers.length"
            :containers="detail.containers"
            @open-item="goToItem"
          />
        </el-col>
      </el-row>
    </template>
  </EntityDetailLayout>
</template>
