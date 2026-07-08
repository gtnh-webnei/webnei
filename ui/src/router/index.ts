import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppShell from '@/app/AppShell.vue'
import FluidListView from '@features/fluid/views/FluidListView.vue'
import ItemListView from '@features/item/views/ItemListView.vue'
import ModListView from '@features/mod/views/ModListView.vue'
import RecipeCategoryListView from '@features/recipe/views/RecipeCategoryListView.vue'
import NotFoundView from '@/app/NotFoundView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppShell,
    children: [
      { path: '', redirect: { name: 'items' } },
      { path: 'items', name: 'items', component: ItemListView },
      { path: 'fluids', name: 'fluids', component: FluidListView },
      { path: 'recipe/categories', name: 'recipe-categories', component: RecipeCategoryListView },
      { path: 'mods', name: 'mods', component: ModListView },
      { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundView },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
