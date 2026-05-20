<script setup lang="ts">
import type { MobSummary } from '@/api/mobs.types'

defineProps<{
  mob: MobSummary
}>()

defineEmits<{
  (e: 'select', mob: MobSummary): void
}>()
</script>

<template>
  <div
    class="mob-card"
    role="button"
    tabindex="0"
    @click="$emit('select', mob)"
    @keydown.enter.prevent="$emit('select', mob)"
    @keydown.space.prevent="$emit('select', mob)"
  >
    <div class="portrait-wrap">
      <img v-if="mob.assetUrl" :src="mob.assetUrl" :alt="mob.displayName" loading="lazy" />
    </div>
    <div class="meta">
      <div class="name" :title="mob.displayName">{{ mob.displayName }}</div>
      <div class="row mod-row">
        <el-tag size="small" type="info" effect="plain" round class="mod-tag">
          {{ mob.modId }}
        </el-tag>
      </div>
      <div class="row attr-row">
        <span class="stat" title="最大生命">♥ {{ mob.maxHealth }}</span>
        <span v-if="mob.armor > 0" class="stat" title="护甲">⛨ {{ mob.armor }}</span>
        <span v-if="mob.immuneToFire" class="badge fire" title="火免疫">火免</span>
        <span v-if="mob.leashable" class="badge leash" title="可栓绳">栓</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mob-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  font: inherit;
  color: inherit;
  text-align: left;
  transition: border-color 0.15s, background 0.15s, box-shadow 0.15s;
}
.mob-card:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.mob-card:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
.portrait-wrap {
  width: 56px;
  height: 56px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
}
.portrait-wrap img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}
.meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.row {
  display: flex;
  gap: 6px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  align-items: center;
  flex-wrap: nowrap;
  overflow: hidden;
}
.mod-tag {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.attr-row {
  min-width: 0;
}
.stat {
  font-variant-numeric: tabular-nums;
}
.badge {
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 3px;
  font-weight: 600;
}
.badge.fire {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}
.badge.leash {
  background: rgba(34, 197, 94, 0.15);
  color: #16a34a;
}
</style>
