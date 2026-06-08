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
import ItemAspectsBlock from './entity-detail/ItemAspectsBlock.vue';
import ItemAttributesCard from './entity-detail/ItemAttributesCard.vue';
import ItemOreDictBlock from './entity-detail/ItemOreDictBlock.vue';
import ItemRelatedFluidsBlock from './entity-detail/ItemRelatedFluidsBlock.vue';
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
  extras.value = null;
  try {
    extras.value = await extrasStore.loadItem(props.datasetId, props.itemVariantId);
  } catch {
    extras.value = null;
  }
}

function pickFluid(fluid: InteractiveFluidRefFluid) {
  entityNavigation.pick(fluid.fluidVariantId, true);
}

function lookupFluid(kind: 'recipe' | 'usage', fluid: InteractiveFluidRefFluid) {
  entityNavigation.lookup(kind, fluid.fluidVariantId, true);
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

          <template v-if="extras">
            <ItemOreDictBlock
              v-if="extras.oreDictNames.length"
              :names="extras.oreDictNames"
            />
            <ItemAspectsBlock
              v-if="extras.aspects.length"
              :aspects="extras.aspects"
            />
          </template>

          <ItemWorldGenerationCard
            v-if="detail.worldGeneration.length"
            :resources="detail.worldGeneration"
            @open="goToWorldGeneration"
          />

          <template v-if="extras">
            <ItemRelatedFluidsBlock
              v-if="extras.relatedFluids.length"
              :fluids="extras.relatedFluids"
              @pick-fluid="pickFluid"
              @lookup-fluid="lookupFluid"
            />
          </template>
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
