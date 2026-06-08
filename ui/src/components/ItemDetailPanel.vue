<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useExtrasStore } from '@/stores/extras';
import { useEntityNavigation } from '@/composables/useEntityNavigation';
import { getItemDetail } from '@/api/items';
import type { ItemDetail } from '@/api/items.types';
import type { ItemExtras } from '@/api/extras.types';
import MinecraftTooltipText from './MinecraftTooltipText.vue';
import type { InteractiveFluidRefFluid } from './InteractiveFluidRef.vue';
import DetailTextCard from './entity-detail/DetailTextCard.vue';
import EntityDetailLayout from './entity-detail/EntityDetailLayout.vue';
import EntityExtrasCard from './entity-detail/EntityExtrasCard.vue';
import ItemAspectsBlock from './entity-detail/ItemAspectsBlock.vue';
import ItemAttributesCard from './entity-detail/ItemAttributesCard.vue';
import ItemFluidContainersBlock from './entity-detail/ItemFluidContainersBlock.vue';
import ItemOreDictBlock from './entity-detail/ItemOreDictBlock.vue';
import ItemWorldGenerationCard from './entity-detail/ItemWorldGenerationCard.vue';

const props = defineProps<{
  datasetId: string;
  itemVariantId: string;
}>();

const router = useRouter();
const entityNavigation = useEntityNavigation(
  router,
  computed(() => props.datasetId),
);
const extrasStore = useExtrasStore();
const detail = ref<ItemDetail | null>(null);
const loading = ref(false);
const error = ref<string | null>(null);

const extras = ref<ItemExtras | null>(null);
const extrasLoading = ref(false);
const extrasError = ref<string | null>(null);

const hasAnyExtras = computed(() => {
  if (!extras.value) return false;
  return (
    extras.value.oreDictNames.length > 0 ||
    extras.value.fluidContainers.length > 0 ||
    extras.value.aspects.length > 0
  );
});

async function load() {
  if (!props.datasetId || !props.itemVariantId) return;
  loading.value = true;
  error.value = null;
  detail.value = null;
  try {
    detail.value = await getItemDetail(props.datasetId, props.itemVariantId);
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}

async function loadExtras() {
  if (!props.datasetId || !props.itemVariantId) return;
  extrasLoading.value = true;
  extrasError.value = null;
  extras.value = null;
  try {
    extras.value = await extrasStore.loadItem(props.datasetId, props.itemVariantId);
  } catch (e) {
    extrasError.value = e instanceof Error ? e.message : String(e);
  } finally {
    extrasLoading.value = false;
  }
}

function goToItem(itemVariantId: string) {
  entityNavigation.pick(itemVariantId, true);
}

function pickFluid(fluid: InteractiveFluidRefFluid) {
  entityNavigation.pick(fluid.fluidVariantId, true);
}

function lookupFluid(kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid) {
  entityNavigation.lookup(kind, fluid.fluidVariantId, true);
}

function goToContainers() {
  router.push({
    name: 'item-containers',
    params: { datasetId: props.datasetId, itemVariantId: props.itemVariantId },
  });
}

function goToWorldGeneration(section: string, key: string) {
  router.push({
    name: 'gt',
    params: { datasetId: props.datasetId, section },
    query: { target: key },
  });
}

watch(
  () => [props.datasetId, props.itemVariantId],
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
    :not-found-text="$t('item.notFound')"
    :skeleton-rows="8"
  >
    <template v-if="detail">
      <el-row :gutter="16">
        <el-col
          :xs="24"
          :md="14"
        >
          <ItemAttributesCard :detail="detail" />

          <el-card
            v-if="detail.tooltipText"
            shadow="never"
            class="tooltip-text-card"
          >
            <MinecraftTooltipText :text="detail.tooltipText" />
          </el-card>

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

          <ItemWorldGenerationCard
            v-if="detail.worldGeneration.length"
            :resources="detail.worldGeneration"
            @open="goToWorldGeneration"
          />

          <EntityExtrasCard
            :loading="extrasLoading"
            :error="extrasError"
            :empty="!hasAnyExtras"
          >
            <template v-if="extras">
              <ItemOreDictBlock
                v-if="extras.oreDictNames.length"
                :names="extras.oreDictNames"
              />
              <ItemFluidContainersBlock
                v-if="extras.fluidContainers.length"
                :containers="extras.fluidContainers"
                :total="extras.fluidContainersTotal"
                @open-item="goToItem"
                @pick-fluid="pickFluid"
                @lookup-fluid="lookupFluid"
                @see-all="goToContainers"
              />
              <ItemAspectsBlock
                v-if="extras.aspects.length"
                :aspects="extras.aspects"
              />
            </template>
          </EntityExtrasCard>
        </el-col>
      </el-row>
    </template>
  </EntityDetailLayout>
</template>

<style scoped>
.tooltip-text-card {
  margin-bottom: 16px;
}
</style>
