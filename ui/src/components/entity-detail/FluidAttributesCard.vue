<script setup lang="ts">
import type { FluidDetail } from '@/api/fluids.types';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  detail: FluidDetail;
  stateLabel: string;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('common.basicAttributes')">
    <el-descriptions
      :column="1"
      border
      size="default"
      class="attribute-descriptions"
    >
      <el-descriptions-item :label="$t('common.variantId')">
        <code>{{ detail.fluidVariantId }}</code>
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.fluidId')">
        <code>{{ detail.fluidId }}</code>
      </el-descriptions-item>
      <el-descriptions-item :label="$t('common.registryName')">
        {{ detail.registryName }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('common.unlocalizedName')">
        {{ detail.unlocalizedName }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('common.mod')">
        {{ detail.modName }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.state')">
        {{ stateLabel }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.temperature')">
        {{ detail.temperature.toLocaleString() }} K
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.density')">
        {{ detail.density.toLocaleString() }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.viscosity')">
        {{ detail.viscosity.toLocaleString() }}
      </el-descriptions-item>
      <el-descriptions-item :label="$t('fluid.luminosity')">
        {{ detail.luminosity }}
      </el-descriptions-item>
      <el-descriptions-item
        v-if="detail.nbtHash"
        :label="$t('common.nbtHash')"
      >
        <code class="small">{{ detail.nbtHash }}</code>
      </el-descriptions-item>
    </el-descriptions>
  </DetailSectionCard>
</template>

<style scoped>
.attribute-descriptions :deep(.el-descriptions__label) {
  width: 1%;
  white-space: nowrap;
}
.attribute-descriptions :deep(.el-descriptions__content) {
  min-width: 0;
}
code {
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 12px;
  word-break: break-all;
}
code.small {
  color: var(--el-text-color-secondary);
}
</style>
