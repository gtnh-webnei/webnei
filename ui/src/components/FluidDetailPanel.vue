<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useExtrasStore } from '@/stores/extras';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import { getFluidDetail } from '@/api/fluids';
import type { FluidDetail } from '@/api/fluids.types';
import type { FluidExtras } from '@/api/extras.types';
import DetailHeroCard from './entity-detail/DetailHeroCard.vue';
import DetailTextCard from './entity-detail/DetailTextCard.vue';
import EntityDetailLayout from './entity-detail/EntityDetailLayout.vue';
import EntityExtrasCard from './entity-detail/EntityExtrasCard.vue';
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
const heroImgFailed = ref(false);

const extras = ref<FluidExtras | null>(null);
const extrasLoading = ref(false);
const extrasError = ref<string | null>(null);

const hasAnyExtras = computed(() => {
  if (!extras.value) return false;
  return extras.value.containers.length > 0 || extras.value.blocks.length > 0;
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

function goToItem(itemVariantId: string) {
  entityNavigation.pick(itemVariantId, true);
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
      <DetailHeroCard
        :title="detail.displayName || detail.registryName"
        :subtitle="detail.modName"
        :asset-url="detail.assetUrl && !heroImgFailed ? detail.assetUrl : null"
        :fallback-text="gaseousShortLabel"
        :fallback-class="detail.gaseous ? 'gaseous' : undefined"
        :icon-class="detail.gaseous ? 'gaseous' : 'fluid'"
        :image-alt="detail.displayName"
        @image-error="heroImgFailed = true"
      >
        <template #tags>
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
            {{ $t('fluid.gaseous') }}
          </el-tag>
        </template>
      </DetailHeroCard>

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

          <EntityExtrasCard
            :loading="extrasLoading"
            :error="extrasError"
            :empty="!hasAnyExtras"
          >
            <template v-if="extras">
              <FluidBlocksBlock
                v-if="extras.blocks.length"
                :blocks="extras.blocks"
                @open-item="goToItem"
              />
              <FluidContainersBlock
                v-if="extras.containers.length"
                :containers="extras.containers"
                @open-item="goToItem"
              />
            </template>
          </EntityExtrasCard>
        </el-col>
      </el-row>
    </template>
  </EntityDetailLayout>
</template>
