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
</script>

<template>
  <el-container class="app-shell">
    <el-header class="app-header">
      <div class="brand">
        <router-link to="/" class="brand-link">{{ t('dataset.brandName') }}</router-link>
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
            <template #title>{{ item.label }}</template>
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
      <DatasetSwitcher />
      <ThemeSwitcher />
    </el-header>
    <el-main class="app-main" :class="{ 'is-full-height': fullHeight }">
      <div :class="fullHeight ? 'page-full' : 'page'">
        <router-view v-slot="{ Component, route: viewRoute }">
          <keep-alive :max="32">
            <component
              v-if="viewRoute.meta.keepAlive"
              :is="Component"
              :key="routeViewKey(viewRoute)"
            />
          </keep-alive>
          <component v-if="!viewRoute.meta.keepAlive" :is="Component" :key="viewRoute.fullPath" />
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
