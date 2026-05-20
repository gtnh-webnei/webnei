import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppShell from '@/layouts/AppShell.vue'
import DatasetIndex from '@/views/DatasetIndex.vue'
import ItemBrowser from '@/views/ItemBrowser.vue'
import ItemContainersView from '@/views/ItemContainersView.vue'
import FluidBrowser from '@/views/FluidBrowser.vue'
import ModBrowser from '@/views/ModBrowser.vue'
import RecipeLookup from '@/views/RecipeLookup.vue'
import CategoryGallery from '@/views/CategoryGallery.vue'
import CategoryDetail from '@/views/CategoryDetail.vue'
import QuestLineList from '@/views/QuestLineList.vue'
import QuestLineCanvas from '@/views/QuestLineCanvas.vue'
import MobBrowser from '@/views/MobBrowser.vue'
import MobDetail from '@/views/MobDetail.vue'
import Placeholder from '@/views/Placeholder.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppShell,
    children: [
      { path: '', name: 'home', component: DatasetIndex },
      {
        path: 'datasets/:datasetId/items',
        name: 'items',
        component: ItemBrowser,
      },
      {
        path: 'datasets/:datasetId/items/:itemVariantId/containers',
        name: 'item-containers',
        component: ItemContainersView,
      },
      {
        path: 'datasets/:datasetId/mods',
        name: 'mods',
        component: ModBrowser,
      },
      {
        path: 'datasets/:datasetId/fluids',
        name: 'fluids',
        component: FluidBrowser,
      },
      {
        path: 'datasets/:datasetId/categories',
        name: 'categories',
        component: CategoryGallery,
      },
      {
        path: 'datasets/:datasetId/categories/:categoryId',
        name: 'category',
        component: CategoryDetail,
      },
      {
        path: 'datasets/:datasetId/recipes/:recipeId',
        name: 'recipe',
        component: Placeholder,
        props: { title: '配方详情', hint: 'M1.2 实现' },
      },
      {
        path: 'datasets/:datasetId/lookup',
        name: 'lookup',
        component: RecipeLookup,
      },
      {
        path: 'datasets/:datasetId/quest-lines',
        name: 'quest-lines',
        component: QuestLineList,
      },
      {
        path: 'datasets/:datasetId/quest-lines/view',
        name: 'quest-line',
        component: QuestLineCanvas,
        meta: { fullHeight: true },
      },
      {
        path: 'datasets/:datasetId/mobs',
        name: 'mobs',
        component: MobBrowser,
      },
      {
        path: 'datasets/:datasetId/mobs/:mobVariantId',
        name: 'mob',
        component: MobDetail,
      },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
