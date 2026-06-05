<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { FluidSummary } from '@/api/fluids.types';

const { t } = useI18n();

const props = defineProps<{
  fluid: FluidSummary;
}>();

const fallbackInitial = computed(() =>
  props.fluid.gaseous ? t('fluid.gaseous') : t('fluid.liquid'),
);

defineEmits<{
  (e: 'select', fluid: FluidSummary): void;
}>();

const imgFailed = ref(false);
</script>

<template>
  <div
    class="fluid-card"
    tabindex="0"
    role="button"
    @click="$emit('select', fluid)"
    @keydown.enter="$emit('select', fluid)"
  >
    <div class="icon-wrap" :class="{ gaseous: fluid.gaseous }">
      <img
        v-if="fluid.assetUrl && !imgFailed"
        :src="fluid.assetUrl"
        :alt="fluid.displayName"
        loading="lazy"
        draggable="false"
        @error="imgFailed = true"
      />
      <div v-else class="initial" :class="{ gaseous: fluid.gaseous }">
        {{ fallbackInitial }}
      </div>
    </div>
    <div class="meta">
      <div class="name">{{ fluid.displayName }}</div>
      <div class="tags">
        <el-tag size="small" type="info" effect="plain" round class="mod-tag">
          {{ fluid.modName }}
        </el-tag>
        <el-tag v-if="fluid.gaseous" size="small" type="warning" effect="plain" round>
          {{ t('fluid.gaseous') }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fluid-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
  cursor: pointer;
  transition:
    border-color 0.15s,
    background 0.15s;
  min-width: 0;
}
.fluid-card:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}
.fluid-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.icon-wrap {
  width: 40px;
  height: 40px;
  background: rgba(33, 130, 230, 0.12);
  border: 1px solid rgba(33, 130, 230, 0.35);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}
.icon-wrap.gaseous {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.45);
}
.icon-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.placeholder {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.initial {
  font-size: 18px;
  font-weight: 700;
  color: rgba(33, 130, 230, 0.9);
}
.initial.gaseous {
  color: rgba(245, 158, 11, 0.95);
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.name {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.id {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}
.mod-tag {
  max-width: 100%;
  min-width: 0;
}
.mod-tag :deep(.el-tag__content) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
