<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import { ArrowDown, Menu as MenuIcon } from '@element-plus/icons-vue'
import DatasetPicker from '@/components/DatasetPicker.vue'
import { useDatasetStore } from '@/stores/dataset'

const { t, te } = useI18n()
const store = useDatasetStore()
const { activeDataset, loading } = storeToRefs(store)

const desktopPickerOpen = ref(false)
const drawerOpen = ref(false)
const drawerSize = 'min(82vw, 300px)'

function languageLabel(language: string): string {
  const key = `language.${language}`
  return te(key) ? t(key) : language
}

const datasetLabel = computed(() => {
  const dataset = activeDataset.value
  if (!dataset) return loading.value ? t('dataset.loading') : t('dataset.empty')
  return `${dataset.packSlug} · ${dataset.packVersion} · ${dataset.variant} · ${languageLabel(dataset.language)}`
})

function closeDesktopPicker() {
  desktopPickerOpen.value = false
}

function closeDrawer() {
  drawerOpen.value = false
}

onMounted(() => {
  if (!store.loaded && !loading.value) {
    void store.load()
  }
})
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <button
        type="button"
        class="menu-button"
        :aria-label="t('common.menu')"
        :title="t('common.menu')"
        @click="drawerOpen = true"
      >
        <el-icon><MenuIcon /></el-icon>
      </button>

      <router-link
        to="/"
        class="brand-link"
      >
        <span
          class="brand-mark"
          aria-hidden="true"
        />
        <span class="brand-title">{{ t('common.appName') }}</span>
      </router-link>

      <div class="header-spacer" />

      <div class="dataset-region">
        <el-popover
          v-model:visible="desktopPickerOpen"
          placement="bottom-end"
          :width="300"
          trigger="click"
          popper-class="dataset-popover"
        >
          <template #reference>
            <button
              type="button"
              class="dataset-trigger"
              :title="t('dataset.switch')"
            >
              <span class="dataset-trigger-label">{{ datasetLabel }}</span>
              <el-icon class="dataset-trigger-icon">
                <ArrowDown />
              </el-icon>
            </button>
          </template>
          <DatasetPicker @select="closeDesktopPicker" />
        </el-popover>
      </div>
    </el-header>

    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>

  <el-drawer
    v-model="drawerOpen"
    direction="ltr"
    :size="drawerSize"
    :with-header="false"
    class="app-drawer"
  >
    <nav class="drawer-body">
      <p class="drawer-brand">
        {{ t('common.appName') }}
      </p>
      <DatasetPicker @select="closeDrawer" />
    </nav>
  </el-drawer>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: var(--app-header-height);
  padding: 0 var(--app-page-padding);
  border-bottom: 3px solid var(--mc-outline);
  background:
    linear-gradient(180deg, rgb(255 255 255 / 0.08), transparent 42%),
    var(--mc-deepslate);
  box-shadow: 0 3px 0 rgb(255 255 255 / 0.08) inset;
}

.menu-button {
  display: none;
  flex: 0 0 auto;
  place-items: center;
  width: 38px;
  height: 38px;
  border: 2px solid var(--mc-outline);
  border-radius: 2px;
  background: var(--mc-panel);
  color: var(--mc-panel-text);
  cursor: pointer;
}

.brand-link {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 10px;
  color: var(--mc-text);
  text-decoration: none;
  white-space: nowrap;
}

.brand-mark {
  width: 36px;
  height: 36px;
  border: 2px solid;
  border-color: #75d88f #1c5f34 #133f24 #a9efb8;
  background:
    linear-gradient(135deg, transparent 0 28%, rgb(255 255 255 / 0.18) 29% 42%, transparent 43%),
    linear-gradient(180deg, #44b86a 0 50%, #2f8e4d 50% 100%);
  box-shadow:
    0 0 0 2px var(--mc-outline),
    0 4px 0 rgb(0 0 0 / 0.45);
}

.brand-title {
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0.06em;
  line-height: 1;
  text-shadow: 2px 2px 0 #000;
}

.header-spacer {
  flex: 1 1 auto;
  min-width: 0;
}

.dataset-region {
  flex: 0 1 auto;
  min-width: 0;
}

.dataset-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 260px;
  height: 38px;
  padding: 0 10px;
  border: 2px solid var(--mc-outline);
  border-radius: 2px;
  background: var(--mc-panel);
  color: var(--mc-panel-text);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.dataset-trigger-label {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dataset-trigger-icon {
  flex: 0 0 auto;
  color: var(--mc-panel-text);
}

.drawer-body {
  display: grid;
  gap: 16px;
  align-content: start;
  padding: 16px;
}

.drawer-brand {
  margin: 0;
  color: var(--mc-text);
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.app-main {
  min-height: calc(100dvh - var(--app-header-height));
  padding: var(--app-page-padding);
  background: transparent;
}

@media (max-width: 640px) {
  .app-header {
    gap: 10px;
  }

  .menu-button {
    display: grid;
  }

  .dataset-region {
    display: none;
  }

  .brand-mark {
    width: 32px;
    height: 32px;
  }

  .brand-title {
    font-size: 18px;
  }
}
</style>
