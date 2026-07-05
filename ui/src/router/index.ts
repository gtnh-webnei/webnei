import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppShell from '@/layouts/AppShell.vue'
import FluidListView from '@/views/FluidListView.vue'
import ItemListView from '@/views/ItemListView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppShell,
    children: [
      { path: '', redirect: { name: 'items' } },
      { path: 'items', name: 'items', component: ItemListView },
      { path: 'fluids', name: 'fluids', component: FluidListView },
      { path: ':pathMatch(.*)*', name: 'not-found', component: NotFoundView },
    ],
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})
