<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import { ArrowLeft } from '@element-plus/icons-vue'
import ModRow from './ModRow.vue'
import type { ModListEntry } from '../types'

const props = defineProps<{ items: ModListEntry[] }>()
defineSlots<{ 'list-header'?: () => unknown; 'list-state'?: () => unknown }>()

const { t } = useI18n()
const selectedId = ref<string | null>(null)
const isMobile = useMediaQuery('(max-width: 720px)')
const detailOpen = ref(false)
const selectedMod = computed(() => props.items.find((mod) => mod.id === selectedId.value) ?? props.items[0])

watch(
  () => props.items.map((mod) => mod.id),
  (ids) => {
    if (!ids.length) {
      selectedId.value = null
      detailOpen.value = false
      return
    }
    if (isMobile.value) {
      if (selectedId.value && !ids.includes(selectedId.value)) {
        selectedId.value = null
        detailOpen.value = false
      }
      return
    }
    if (!selectedId.value || !ids.includes(selectedId.value)) selectedId.value = ids[0]
  },
  { immediate: true },
)

function selectMod(id: string) {
  selectedId.value = id
  detailOpen.value = true
}

function backToList() {
  detailOpen.value = false
}

const showDetail = computed(() => !!selectedMod.value && (!isMobile.value || detailOpen.value))
const showList = computed(() => !isMobile.value || !detailOpen.value)
</script>

<template>
  <div class="forge-mod-console">
    <Transition name="mod-pane">
      <section
        v-if="showList"
        class="forge-mod-list-module"
      >
        <header class="forge-mod-list-toolbar">
          <slot name="list-header" />
        </header>
        <div class="forge-mod-list-well">
          <slot
            v-if="!items.length"
            name="list-state"
          />
          <ol
            v-else
            class="forge-mod-list"
          >
            <ModRow
              v-for="mod in items"
              :key="mod.id"
              :entry="mod"
              :selected="!isMobile && selectedMod?.id === mod.id"
              @select="selectMod"
            />
          </ol>
        </div>
      </section>
    </Transition>

    <Transition name="mod-detail">
      <aside
        v-if="showDetail"
        class="forge-mod-detail-module"
      >
        <div class="forge-mod-detail-well">
          <button
            v-if="isMobile"
            type="button"
            class="forge-mod-back"
            @click="backToList"
          >
            <el-icon class="forge-mod-back-icon">
              <ArrowLeft />
            </el-icon>
            {{ t('common.back') }}
          </button>
          <h3 class="forge-mod-title">
            {{ selectedMod.name }}
          </h3>
          <dl class="forge-mod-fields">
            <div class="forge-mod-field">
              <dt>{{ t('catalog.modVersion') }}</dt><dd>{{ selectedMod.version }}</dd>
            </div>
            <div class="forge-mod-field">
              <dt>{{ t('catalog.modIdentifier') }}</dt><dd class="is-code">
                {{ selectedMod.modId }}
              </dd>
            </div>
            <div class="forge-mod-field">
              <dt>{{ t('catalog.modSourceType') }}</dt><dd class="is-code">
                {{ selectedMod.sourceType }}
              </dd>
            </div>
            <div class="forge-mod-field">
              <dt>{{ t('catalog.modSourceFileName') }}</dt><dd class="is-code is-break">
                {{ selectedMod.sourceFileName }}
              </dd>
            </div>
            <div class="forge-mod-field">
              <dt>{{ t('catalog.modSourceSha256') }}</dt><dd class="is-code is-break">
                {{ selectedMod.sourceSha256 }}
              </dd>
            </div>
          </dl>
        </div>
      </aside>
    </Transition>
  </div>
</template>

<style scoped>
.forge-mod-console {
  display: grid;
  grid-template-columns: minmax(220px, 31%) minmax(0, 1fr);
  gap: 10px;
  height: 100%;
  min-width: 0;
  min-height: 0;
  padding: 8px 4px 5px 0;
}

.forge-mod-list-module,
.forge-mod-detail-module {
  min-width: 0;
  min-height: 0;
  border: 2px solid;
  border-color: var(--mc-panel-hi) var(--mc-panel-low) var(--mc-panel-low) var(--mc-panel-hi);
  background: var(--mc-panel);
}

.forge-mod-list-module {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 8px;
  margin-left: -20px;
  padding: 9px 8px 8px 12px;
  box-shadow: 3px 3px 0 rgb(0 0 0 / 0.26);
}

.forge-mod-list-toolbar {
  display: grid;
  gap: 7px;
  min-width: 0;
}

.forge-mod-list-well,
.forge-mod-detail-well {
  min-width: 0;
  min-height: 0;
  border: 2px solid;
  border-color: var(--mc-panel-low) var(--mc-panel-hi) var(--mc-panel-hi) var(--mc-panel-low);
  background: var(--forge-screen-bg);
}

.forge-mod-list-well { overflow: hidden; }

.forge-mod-list {
  display: grid;
  grid-auto-rows: var(--forge-slot-height);
  gap: var(--forge-slot-gap);
  height: 100%;
  min-width: 0;
  min-height: 0;
  margin: 0;
  padding: 6px;
  overflow: auto;
  list-style: none;
  scrollbar-color: var(--forge-scroll-thumb) var(--forge-scroll-track);
  scrollbar-width: thin;
}

.forge-mod-detail-module {
  margin: 18px -18px 13px 0;
  padding: 8px;
  box-shadow: 4px 4px 0 rgb(0 0 0 / 0.3);
}

.forge-mod-detail-well {
  display: grid;
  gap: 14px;
  align-content: start;
  height: 100%;
  padding: 14px;
  overflow: auto;
  color: var(--forge-detail-line);
  scrollbar-color: var(--forge-scroll-thumb) var(--forge-scroll-track);
  scrollbar-width: thin;
}

.forge-mod-list::-webkit-scrollbar,
.forge-mod-detail-well::-webkit-scrollbar { width: var(--forge-scrollbar-width); }
.forge-mod-list::-webkit-scrollbar-track,
.forge-mod-detail-well::-webkit-scrollbar-track { background: var(--forge-scroll-track); }
.forge-mod-list::-webkit-scrollbar-thumb,
.forge-mod-detail-well::-webkit-scrollbar-thumb { border-right: 1px solid var(--forge-scroll-thumb-hi); background: var(--forge-scroll-thumb); }

.forge-mod-title,
.forge-mod-fields { margin: 0; }
.forge-mod-title { min-width: 0; padding-bottom: 10px; overflow-wrap: anywhere; border-bottom: 1px solid var(--forge-scroll-thumb); color: var(--forge-list-text); font-size: var(--forge-detail-title-size); font-weight: 800; line-height: 1.15; text-shadow: 1px 1px 0 var(--forge-list-selected-inner); }
.forge-mod-fields { display: grid; gap: 10px; }
.forge-mod-field { display: grid; grid-template-columns: 112px minmax(0, 1fr); gap: 12px; align-items: baseline; min-width: 0; }
.forge-mod-field dt { color: var(--forge-list-muted); font-size: calc(var(--forge-detail-line-size) - 1px); font-weight: 800; line-height: 1.4; }
.forge-mod-field dd { min-width: 0; margin: 0; color: var(--forge-detail-line); font-size: var(--forge-detail-line-size); font-weight: 700; line-height: 1.4; }
.forge-mod-field .is-break { overflow-wrap: anywhere; word-break: break-word; }

.forge-mod-back { display: inline-flex; align-items: center; gap: 4px; justify-self: start; padding: 4px 6px; border: 0; background: transparent; color: var(--forge-list-muted); font: inherit; font-size: 13px; font-weight: 800; cursor: pointer; }
.forge-mod-back:hover { color: var(--forge-list-text); }
.forge-mod-back:focus-visible { color: var(--forge-list-text); outline: 2px solid var(--mc-gold); outline-offset: 2px; }
.forge-mod-back-icon { font-size: 15px; }

.mod-pane-enter-active,
.mod-pane-leave-active,
.mod-detail-enter-active,
.mod-detail-leave-active { transition: opacity 180ms ease, transform 180ms ease; }
.mod-pane-enter-from,
.mod-pane-leave-to { transform: translateX(-6%); opacity: 0; }
.mod-detail-enter-from,
.mod-detail-leave-to { transform: translateX(6%); opacity: 0; }

@media (max-width: 720px) {
  .forge-mod-console { grid-template-columns: minmax(0, 1fr); grid-template-rows: minmax(0, 1fr); gap: 0; padding: 0; }
  .forge-mod-list-module,
  .forge-mod-detail-module { margin: 0; }
  .forge-mod-list-module { padding: 8px; }
  .forge-mod-detail-module { padding: 8px; }
  .forge-mod-field { grid-template-columns: minmax(0, 1fr); gap: 2px; }
  .forge-mod-title { font-size: 18px; }
}

@media (max-width: 520px) {
  .forge-mod-detail-well { padding: 10px; }
}

@media (prefers-reduced-motion: reduce) {
  .mod-pane-enter-active,
  .mod-pane-leave-active,
  .mod-detail-enter-active,
  .mod-detail-leave-active { transition: none; }
  .mod-pane-enter-from,
  .mod-pane-leave-to,
  .mod-detail-enter-from,
  .mod-detail-leave-to { transform: none; }
}
</style>
