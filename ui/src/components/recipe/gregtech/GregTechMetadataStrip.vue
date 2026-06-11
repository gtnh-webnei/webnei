<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue';
import type { MetadataValue, RecipeSlot } from '@/api/recipes.types';
import {
  buildCtx,
  extractHandlerName,
  loadDisplaySpec,
  loadDisplaySpecMessages,
  renderRecipe,
  type DisplayItem,
  type DisplaySpec,
} from './displaySpec';
import { useI18n } from 'vue-i18n';

const props = defineProps<{
  metadata: Record<string, MetadataValue>;
  slots: RecipeSlot[];
  /** spec 文件 URL（来自 dataset.displaySpecUrl，由后端 AssetUrlBuilder 拼出） */
  specUrl: string | null;
  /** spec i18n 文件 URL（来自 dataset.displaySpecMessagesUrl） */
  specMessagesUrl: string | null;
  /** category.handlerId，形如 "gregtech:macerator" */
  handlerId: string | null | undefined;
}>();

const emit = defineEmits<{
  (e: 'openQuest', questId: string): void;
}>();

const spec = ref<DisplaySpec | null>(null);
const { t, locale } = useI18n();
watchEffect(async () => {
  const activeLocale = String(locale.value);
  const [loadedSpec] = await Promise.all([
    loadDisplaySpec(props.specUrl),
    loadDisplaySpecMessages(props.specMessagesUrl, activeLocale),
  ]);
  spec.value = loadedSpec;
});

const items = computed<DisplayItem[]>(() => {
  if (!spec.value) return [];
  const handler = extractHandlerName(props.handlerId);
  if (!handler) return [];
  const ctx = buildCtx(handler, props.metadata, props.slots);
  return renderRecipe({ spec: spec.value, ctx, t });
});

function stripColorCode(s: string): string {
  return s.replace(/§[0-9a-fk-or]/gi, '');
}

function openAction(item: DisplayItem) {
  if (item.action === 'open_quest' && item.targetId) {
    emit('openQuest', item.targetId);
  }
}

function colorCodeToCss(code: string | undefined): string {
  if (!code) return '';
  const styles: string[] = [];
  if (/§[0-9a-f]/i.test(code)) {
    const m = code.match(/§([0-9a-f])/i)?.[1]?.toLowerCase();
    const colors: Record<string, string> = {
      '0': '#000',
      '1': '#0000aa',
      '2': '#00aa00',
      '3': '#00aaaa',
      '4': '#aa0000',
      '5': '#aa00aa',
      '6': '#ffaa00',
      '7': '#aaaaaa',
      '8': '#555555',
      '9': '#5555ff',
      a: '#55ff55',
      b: '#55ffff',
      c: '#ff5555',
      d: '#ff55ff',
      e: '#ffff55',
      f: '#fff',
    };
    if (m && colors[m]) styles.push(`color: ${colors[m]}`);
  }
  if (/§l/i.test(code)) styles.push('font-weight: 700');
  if (/§n/i.test(code)) styles.push('text-decoration: underline');
  if (/§o/i.test(code)) styles.push('font-style: italic');
  return styles.join('; ');
}
</script>

<template>
  <div v-if="items.length" class="display-strip">
    <dl class="meta-list">
      <template v-for="(item, idx) in items" :key="idx">
        <template v-if="item.label === null">
          <dt class="label freetext-label" />
          <dd
            class="value freetext"
            :class="{
              'preserve-newlines': item.preserveNewlines,
              actionable: item.action && item.targetId,
            }"
            :style="colorCodeToCss(item.colorCode)"
            :role="item.action && item.targetId ? 'button' : undefined"
            :tabindex="item.action && item.targetId ? 0 : undefined"
            @click="openAction(item)"
            @keydown.enter.prevent="openAction(item)"
            @keydown.space.prevent="openAction(item)"
          >
            {{ stripColorCode(item.value) }}
          </dd>
        </template>
        <template v-else>
          <dt class="label">
            {{ item.label }}
          </dt>
          <dd
            class="value"
            :class="{
              'preserve-newlines': item.preserveNewlines,
              actionable: item.action && item.targetId,
            }"
            :style="colorCodeToCss(item.colorCode)"
            :role="item.action && item.targetId ? 'button' : undefined"
            :tabindex="item.action && item.targetId ? 0 : undefined"
            @click="openAction(item)"
            @keydown.enter.prevent="openAction(item)"
            @keydown.space.prevent="openAction(item)"
          >
            {{ stripColorCode(item.value) }}
          </dd>
        </template>
      </template>
    </dl>
  </div>
</template>

<style scoped>
.display-strip {
  display: flex;
  flex-direction: column;
}
.meta-list {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 4px 8px;
  margin: 0;
  font-size: 12px;
  line-height: 1.4;
}
.label {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.value {
  margin: 0;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.freetext-label {
  display: none;
}
.freetext {
  grid-column: 1 / -1;
  color: var(--el-text-color-regular);
  font-weight: 600;
}
.value.preserve-newlines {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  overflow: visible;
  text-overflow: clip;
}
.value.actionable {
  color: var(--el-color-primary);
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
