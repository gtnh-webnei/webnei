<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import { ArrowLeft } from '@element-plus/icons-vue'
import ModRow from './ModRow.vue'
import type { ModListEntry } from '../types'

const props = defineProps<{
  items: ModListEntry[]
}>()

const { t } = useI18n()
const selectedId = ref<string | null>(null)

// 移动端：列表与详情一次只显示一个；桌面端始终左右并列。
const isMobile = useMediaQuery('(max-width: 720px)')
// 移动端是否已进入详情视图（点行后为 true，返回后为 false）。
const detailOpen = ref(false)

const selectedMod = computed(() => props.items.find((mod) => mod.id === selectedId.value) ?? props.items[0])

// 桌面端默认选中首项预览；移动端不预选，先看列表。
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

    if (!selectedId.value || !ids.includes(selectedId.value)) {
      selectedId.value = ids[0]
    }
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

// 详情面板可见：桌面端只要有选中项；移动端需已点开。
const showDetail = computed(() => !!selectedMod.value && (!isMobile.value || detailOpen.value))
// 列表可见：桌面端始终；移动端仅未进入详情时。
const showList = computed(() => !isMobile.value || !detailOpen.value)
</script>

<template>
  <div
    class="forge-mod-screen"
    :class="{ 'is-detail': isMobile && detailOpen }"
  >
    <Transition name="mod-pane">
      <section
        v-if="showList"
        class="forge-mod-list-pane"
      >
        <ol class="forge-mod-list">
          <ModRow
            v-for="mod in items"
            :key="mod.id"
            :entry="mod"
            :selected="!isMobile && selectedMod?.id === mod.id"
            @select="selectMod"
          />
        </ol>
      </section>
    </Transition>

    <Transition name="mod-detail">
      <aside
        v-if="showDetail"
        class="forge-mod-detail"
      >
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
            <dt>{{ t('catalog.modVersion') }}</dt>
            <dd>{{ selectedMod.version }}</dd>
          </div>
          <div class="forge-mod-field">
            <dt>{{ t('catalog.modIdentifier') }}</dt>
            <dd class="is-code">
              {{ selectedMod.modId }}
            </dd>
          </div>
          <div class="forge-mod-field">
            <dt>{{ t('catalog.modSourceType') }}</dt>
            <dd class="is-code">
              {{ selectedMod.sourceType }}
            </dd>
          </div>
          <div class="forge-mod-field">
            <dt>{{ t('catalog.modSourceFileName') }}</dt>
            <dd class="is-code is-break">
              {{ selectedMod.sourceFileName }}
            </dd>
          </div>
          <div class="forge-mod-field">
            <dt>{{ t('catalog.modSourceSha256') }}</dt>
            <dd class="is-code is-break">
              {{ selectedMod.sourceSha256 }}
            </dd>
          </div>
        </dl>
      </aside>
    </Transition>
  </div>
</template>

<style scoped>
.forge-mod-screen {
  display: grid;
  grid-template-columns: minmax(170px, 260px) minmax(0, 1fr);
  gap: 20px;
  height: 100%;
  min-width: 0;
  min-height: 360px;
  overflow: hidden;
  padding: 10px 20px 14px 10px;
  background: var(--forge-screen-bg);
}

.forge-mod-list-pane {
  min-width: 0;
  min-height: 0;
}

.forge-mod-list {
  display: grid;
  grid-auto-rows: var(--forge-slot-height);
  gap: var(--forge-slot-gap);
  min-width: 0;
  height: 100%;
  min-height: 0;
  margin: 0;
  padding: 4px 7px 4px 0;
  overflow: auto;
  list-style: none;
  scrollbar-color: var(--forge-scroll-thumb) var(--forge-scroll-track);
  scrollbar-width: thin;
}

.forge-mod-list::-webkit-scrollbar,
.forge-mod-detail::-webkit-scrollbar {
  width: var(--forge-scrollbar-width);
}

.forge-mod-list::-webkit-scrollbar-track,
.forge-mod-detail::-webkit-scrollbar-track {
  background: var(--forge-scroll-track);
}

.forge-mod-list::-webkit-scrollbar-thumb,
.forge-mod-detail::-webkit-scrollbar-thumb {
  border-right: 1px solid var(--forge-scroll-thumb-hi);
  background: var(--forge-scroll-thumb);
}

.forge-mod-detail {
  display: grid;
  gap: 16px;
  align-content: start;
  min-width: 0;
  overflow: auto;
  padding-top: 3px;
  color: var(--forge-detail-line);
  scrollbar-color: var(--forge-scroll-thumb) var(--forge-scroll-track);
  scrollbar-width: thin;
}

.forge-mod-title,
.forge-mod-fields {
  margin: 0;
}

.forge-mod-title {
  min-width: 0;
  padding-bottom: 12px;
  overflow-wrap: anywhere;
  border-bottom: 1px solid var(--forge-scroll-thumb);
  color: var(--forge-list-text);
  font-size: var(--forge-detail-title-size);
  font-weight: 800;
  line-height: 1.15;
  text-shadow: 1px 1px 0 var(--forge-list-selected-inner);
}

.forge-mod-fields {
  display: grid;
  gap: 10px;
}

.forge-mod-field {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  gap: 14px;
  align-items: baseline;
  min-width: 0;
}

.forge-mod-field dt {
  color: var(--forge-list-muted);
  font-size: calc(var(--forge-detail-line-size) - 1px);
  font-weight: 800;
  line-height: 1.4;
}

.forge-mod-field dd {
  min-width: 0;
  margin: 0;
  color: var(--forge-detail-line);
  font-size: var(--forge-detail-line-size);
  font-weight: 700;
  line-height: 1.4;
}

.forge-mod-field .is-code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.forge-mod-field .is-break {
  overflow-wrap: anywhere;
}

@media (max-width: 720px) {
  /* 移动端：单栏铺满，列表与详情一次只显示一个 */
  .forge-mod-screen {
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: minmax(0, 1fr);
    gap: 0;
    padding: 10px;
  }

  .forge-mod-detail {
    height: 100%;
    padding-top: 0;
  }

  .forge-mod-title {
    padding-bottom: 10px;
    font-size: 18px;
  }

  .forge-mod-field {
    grid-template-columns: minmax(0, 1fr);
    gap: 2px;
  }
}

/* 返回：贴合深色详情面板自身语言，非浅灰浮层 */
.forge-mod-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  justify-self: start;
  margin-left: -4px;
  padding: 4px 6px;
  border: 0;
  background: transparent;
  color: var(--forge-list-muted);
  font: inherit;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: color 90ms ease;
}

.forge-mod-back:hover,
.forge-mod-back:focus-visible {
  color: var(--forge-list-text);
  outline: none;
}

.forge-mod-back-icon {
  font-size: 15px;
}

/* 列表↔详情切换：列表向左滑出、详情从右滑入 */
.mod-pane-enter-active,
.mod-pane-leave-active,
.mod-detail-enter-active,
.mod-detail-leave-active {
  transition:
    transform 180ms ease,
    opacity 180ms ease;
}

.mod-pane-enter-from,
.mod-pane-leave-to {
  transform: translateX(-6%);
  opacity: 0;
}

.mod-detail-enter-from,
.mod-detail-leave-to {
  transform: translateX(6%);
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .mod-pane-enter-active,
  .mod-pane-leave-active,
  .mod-detail-enter-active,
  .mod-detail-leave-active {
    transition: none;
  }

  .mod-pane-enter-from,
  .mod-pane-leave-to,
  .mod-detail-enter-from,
  .mod-detail-leave-to {
    transform: none;
  }
}
</style>
