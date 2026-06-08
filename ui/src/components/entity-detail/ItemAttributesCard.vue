<script setup lang="ts">
import type { ItemDetail } from '@/api/items.types';
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  detail: ItemDetail;
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
        <code>{{ detail.itemVariantId }}</code>
      </el-descriptions-item>
      <el-descriptions-item :label="$t('item.itemId')">
        <code>{{ detail.itemId }}</code>
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
      <el-descriptions-item :label="$t('common.damageMeta')">
        {{ detail.damage }}<span v-if="detail.maxDamage > 0"> / {{ detail.maxDamage }}</span>
      </el-descriptions-item>
      <el-descriptions-item :label="$t('item.maxStackSize')">
        {{ detail.maxStackSize }}
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
