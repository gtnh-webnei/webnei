import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFluidExtras, getItemExtras } from '@/api/extras'
import type { FluidExtras, ItemExtras } from '@/api/extras.types'

type Kind = 'item' | 'fluid'

function cacheKey(datasetId: string, kind: Kind, id: string): string {
  return `${datasetId}|${kind}|${id}`
}

export const useExtrasStore = defineStore('extras', () => {
  const itemCache = ref(new Map<string, ItemExtras>())
  const fluidCache = ref(new Map<string, FluidExtras>())
  const inflight = ref(new Map<string, Promise<unknown>>())

  function clearDataset(datasetId: string) {
    for (const key of [...itemCache.value.keys()]) {
      if (key.startsWith(`${datasetId}|`)) itemCache.value.delete(key)
    }
    for (const key of [...fluidCache.value.keys()]) {
      if (key.startsWith(`${datasetId}|`)) fluidCache.value.delete(key)
    }
  }

  async function loadItem(datasetId: string, itemVariantId: string): Promise<ItemExtras> {
    const key = cacheKey(datasetId, 'item', itemVariantId)
    const cached = itemCache.value.get(key)
    if (cached) return cached
    const existing = inflight.value.get(key) as Promise<ItemExtras> | undefined
    if (existing) return existing
    const p = getItemExtras(datasetId, itemVariantId)
      .then((res) => {
        itemCache.value.set(key, res)
        return res
      })
      .finally(() => {
        inflight.value.delete(key)
      })
    inflight.value.set(key, p)
    return p
  }

  async function loadFluid(datasetId: string, fluidVariantId: string): Promise<FluidExtras> {
    const key = cacheKey(datasetId, 'fluid', fluidVariantId)
    const cached = fluidCache.value.get(key)
    if (cached) return cached
    const existing = inflight.value.get(key) as Promise<FluidExtras> | undefined
    if (existing) return existing
    const p = getFluidExtras(datasetId, fluidVariantId)
      .then((res) => {
        fluidCache.value.set(key, res)
        return res
      })
      .finally(() => {
        inflight.value.delete(key)
      })
    inflight.value.set(key, p)
    return p
  }

  return { loadItem, loadFluid, clearDataset }
})
