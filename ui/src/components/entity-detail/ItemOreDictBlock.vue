<script setup lang="ts">
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  names: string[];
}>();
</script>

<template>
  <DetailSectionCard :title="$t('item.oreDictEntries')">
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
  </DetailSectionCard>
</template>

<style scoped>
.ore-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
</style>
