<script setup lang="ts">
import DetailSectionCard from './DetailSectionCard.vue';

defineProps<{
  loading: boolean;
  error: string | null;
  empty: boolean;
}>();
</script>

<template>
  <DetailSectionCard :title="$t('common.extrasInfo')">
    <el-skeleton
      v-if="loading"
      :rows="3"
      animated
    />
    <el-alert
      v-else-if="error"
      :title="error"
      type="error"
      :closable="false"
    />
    <template v-else>
      <slot />
      <div
        v-if="empty"
        class="extras-empty"
      >
        {{ $t('common.noExtras') }}
      </div>
    </template>
  </DetailSectionCard>
</template>

<style scoped>
.extras-empty {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
