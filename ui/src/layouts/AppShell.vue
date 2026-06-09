<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, type RouteLocationNormalizedLoaded } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useI18n } from 'vue-i18n';
import { useDatasetStore } from '@/stores/dataset';
import DatasetSwitcher from '@/components/DatasetSwitcher.vue';
import ThemeSwitcher from '@/components/ThemeSwitcher.vue';

const { t } = useI18n();
const route = useRoute();
const store = useDatasetStore();
const { activeDatasetId } = storeToRefs(store);

const fullHeight = computed(() => Boolean(route.meta?.fullHeight));

const nav = computed(() => {
  const id = activeDatasetId.value ?? route.params.datasetId;
  if (!id || typeof id !== 'string') return [];
  const enc = encodeURIComponent(id);
  return [
    { to: `/datasets/${enc}/items`, label: t('nav.items') },
    { to: `/datasets/${enc}/fluids`, label: t('nav.fluids') },
    { to: `/datasets/${enc}/categories`, label: t('nav.categories') },
    { to: `/datasets/${enc}/quest-lines`, label: t('nav.quests') },
    { to: `/datasets/${enc}/mobs`, label: t('nav.mobs') },
    {
      label: t('nav.gt'),
      index: `/datasets/${enc}/gt`,
      children: [{ to: `/datasets/${enc}/gt/ore-veins`, label: t('gtResource.pageTitle') }],
    },
    { to: `/datasets/${enc}/mods`, label: t('nav.mods') },
  ];
});

const activeNav = computed(() => {
  const id = activeDatasetId.value ?? route.params.datasetId;
  if (route.path.includes('/gt/') && typeof id === 'string') {
    return `/datasets/${encodeURIComponent(id)}/gt`;
  }
  return route.path;
});

function routeViewKey(viewRoute: RouteLocationNormalizedLoaded): string {
  return String(viewRoute.name ?? viewRoute.path);
}

function nonCachedRouteViewKey(viewRoute: RouteLocationNormalizedLoaded): string {
  if (viewRoute.name === 'lookup') {
    return `${String(viewRoute.name)}:${String(viewRoute.params.datasetId ?? '')}:${String(viewRoute.query.target ?? '')}`;
  }
  return viewRoute.fullPath;
}
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="brand">
        <router-link to="/" class="brand-link">
          {{ t('dataset.brandName') }}
        </router-link>
      </div>
      <el-menu
        mode="horizontal"
        :ellipsis="false"
        :default-active="activeNav"
        router
        class="nav-menu"
      >
        <template v-for="item in nav" :key="item.to ?? item.index">
          <el-sub-menu v-if="item.children" :index="item.index">
            <template #title>
              {{ item.label }}
            </template>
            <el-menu-item v-for="child in item.children" :key="child.to" :index="child.to">
              {{ child.label }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.to">
            {{ item.label }}
          </el-menu-item>
        </template>
      </el-menu>
      <div class="spacer" />
      <div class="header-actions">
        <DatasetSwitcher />
        <ThemeSwitcher />
        <span class="header-divider" aria-hidden="true" />
        <a
          class="github-link"
          href="https://github.com/gtnh-webnei/webnei"
          target="_blank"
          rel="noopener noreferrer"
          :title="t('common.githubRepository')"
          :aria-label="t('common.githubRepository')"
        >
          <svg viewBox="0 0 16 16" aria-hidden="true" class="github-icon">
            <path
              fill="currentColor"
              d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27s1.36.09 2 .27c1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8"
            />
          </svg>
        </a>
      </div>
    </el-header>
    <el-main class="app-main" :class="{ 'is-full-height': fullHeight }">
      <div :class="fullHeight ? 'page-full' : 'page'">
        <router-view v-slot="{ Component, route: viewRoute }">
          <keep-alive :max="32">
            <component
              :is="Component"
              v-if="viewRoute.meta.keepAlive"
              :key="routeViewKey(viewRoute)"
            />
          </keep-alive>
          <component
            :is="Component"
            v-if="!viewRoute.meta.keepAlive"
            :key="nonCachedRouteViewKey(viewRoute)"
          />
        </router-view>
      </div>
    </el-main>
  </el-container>
</template>

<style scoped>
.app-shell {
  height: 100vh;
}
.app-header {
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 0 16px;
}
.brand {
  font-weight: 600;
  font-size: 18px;
  margin-right: 8px;
}
.brand-link {
  color: inherit;
  text-decoration: none;
}
.nav-menu {
  flex: 0 1 auto;
  border-bottom: none;
}
.spacer {
  flex: 1;
}
.header-actions {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: max-content;
  align-items: center;
  gap: 10px;
}
.header-divider {
  width: 1px;
  height: 28px;
  background: var(--el-border-color-lighter);
}
.github-link {
  display: inline-grid;
  place-items: center;
  width: 32px;
  height: 32px;
  color: var(--el-text-color-regular);
  text-decoration: none;
}
.github-link:hover {
  color: var(--el-color-primary);
}
.github-icon {
  width: 24px;
  height: 24px;
}
.app-main {
  padding: 16px;
  background: var(--el-bg-color-page);
  overflow: auto;
}
.page {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}
.page-full {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-height: 0;
  width: 100%;
}
.app-main.is-full-height {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.page-full > :deep(*) {
  flex: 1;
  min-height: 0;
}
</style>
