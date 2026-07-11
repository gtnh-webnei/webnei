<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { useDatasetStore } from '@features/dataset/store'
import SearchHelp, { type SearchHelpItem } from '@shared/components/SearchHelp.vue'
import { useSearchQuery } from '@shared/composables/useSearchQuery'
import McInput from '@shared/ui/McInput.vue'
import ModList from '../components/ModList.vue'
import { listMods } from '../api'

const { t } = useI18n()
const { activeDatasetId } = storeToRefs(useDatasetStore())
const state = useSearchQuery(activeDatasetId, listMods)
const hasDataset = computed(() => activeDatasetId.value != null)
const helpItems: SearchHelpItem[] = [{ token: 'text', key: 'catalog.searchHelpModDefault' }]
</script>

<template>
  <section class="mod-machine-viewport">
    <section
      class="mod-machine-frame"
      :class="{ 'is-loading': state.loading.value && state.items.value.length > 0 }"
    >
      <ModList :items="state.items.value">
        <template #list-header>
          <div class="mod-catalog-heading">
            <h1>{{ t('catalog.mods') }}</h1>
            <span>{{ t('catalog.resultCount', { count: state.total.value }) }}</span>
          </div>
          <div class="mod-search">
            <McInput
              v-model="state.query.value"
              clearable
              :placeholder="t('catalog.searchPlaceholder')"
            />
            <SearchHelp :items="helpItems" />
          </div>
        </template>
        <template #list-state>
          <p
            class="mod-catalog-state"
            :class="{ 'is-error': state.error.value }"
          >
            <template v-if="!hasDataset">
              {{ t('dataset.empty') }}
            </template>
            <template v-else-if="state.error.value">
              {{ state.error.value }}
            </template>
            <template v-else-if="state.loading.value">
              {{ t('catalog.loading') }}
            </template>
            <template v-else>
              {{ t('catalog.empty') }}
            </template>
          </p>
        </template>
      </ModList>
    </section>
  </section>
</template>

<style scoped>
.mod-machine-viewport {
  height: calc(100dvh - var(--app-header-height) - var(--app-page-padding) - var(--app-page-padding));
  min-width: 0;
  min-height: 0;
}

.mod-machine-frame {
  height: 100%;
  min-width: 0;
  min-height: 0;
  padding: 12px;
  border: 2px solid;
  border-color: var(--mc-panel-hi) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-hi);
  border-radius: 2px;
  background: var(--mc-panel);
  box-shadow: 4px 5px 0 rgb(0 0 0 / 0.38);
  transition: opacity 120ms linear;
}

.mod-machine-frame.is-loading {
  opacity: 0.72;
}

.mod-catalog-heading {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.mod-catalog-heading h1,
.mod-catalog-heading span,
.mod-catalog-state {
  margin: 0;
}

.mod-catalog-heading h1 {
  color: var(--mc-panel-text);
  font-size: 16px;
  font-weight: 900;
  letter-spacing: 0.03em;
}

.mod-catalog-heading span {
  color: var(--app-surface-muted);
  font-size: 11px;
  font-weight: 800;
}

.mod-search {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.mod-search > :first-child {
  flex: 1 1 auto;
  min-width: 0;
}

.mod-catalog-state {
  display: grid;
  place-items: center;
  min-height: 120px;
  padding: 12px;
  color: var(--forge-list-muted);
  font-size: 13px;
  font-weight: 800;
  text-align: center;
}

.mod-catalog-state.is-error {
  color: var(--mc-redstone);
}

@media (max-width: 720px) {
  .mod-machine-frame {
    padding: 8px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .mod-machine-frame {
    transition: none;
  }
}
</style>
