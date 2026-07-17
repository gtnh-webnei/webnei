<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import McButton from '@shared/ui/McButton.vue'
import McTooltip from '@shared/ui/McTooltip.vue'

/** 搜索语法帮助浮层。items 由各领域提供（token 展示 + i18n 文案 key）。 */
export interface SearchHelpItem {
  token: string
  key: string
}

defineProps<{
  items: SearchHelpItem[]
}>()

const { t } = useI18n()
</script>

<template>
  <McTooltip
    :data="{ kind: 'custom' }"
    placement="bottom-end"
  >
    <template #content>
      <div class="search-help">
        <p class="search-help-title">
          {{ t('catalog.searchHelpTitle') }}
        </p>
        <ul class="search-help-list">
          <li
            v-for="item in items"
            :key="item.token"
          >
            <span class="search-help-token">{{ item.token }}</span>
            <span>{{ t(item.key) }}</span>
          </li>
        </ul>
      </div>
    </template>
    <McButton
      class="search-help-button"
      :aria-label="t('catalog.searchHelpLabel')"
    >
      ?
    </McButton>
  </McTooltip>
</template>

<style scoped>
.search-help-button {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  font-size: 15px;
  cursor: help;
}

.search-help-title {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 900;
}

.search-help-list {
  display: grid;
  gap: 6px;
  margin: 0;
  padding: 0;
  font-size: 12px;
  line-height: 1.4;
  list-style: none;
}

.search-help-list li {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
}

.search-help-token {
  font-family: inherit;
  font-weight: 900;
  white-space: nowrap;
}
</style>
