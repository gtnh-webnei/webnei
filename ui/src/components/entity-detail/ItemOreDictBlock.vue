<script setup lang="ts">
import ExtrasBlock from './ExtrasBlock.vue';

defineProps<{
  names: string[];
}>();
</script>

<template>
  <ExtrasBlock
    :title="$t('item.oreDictEntries')"
    :count="names.length"
  >
    <div class="ore-tags">
      <el-tag
        v-for="name in names"
        :key="name"
        size="small"
        type="success"
        effect="plain"
        round
      >
        {{ name }}
      </el-tag>
    </div>
  </ExtrasBlock>
</template>

<style scoped>
.ore-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
