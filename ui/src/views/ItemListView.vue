<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import CatalogBrowser from '@/components/catalog/CatalogBrowser.vue'
import { listItems } from '@/api/item'
import type { CatalogEntry, CatalogQueryParams, PageResponse } from '@/api/types'

const { t } = useI18n()

async function fetchItems(params: CatalogQueryParams): Promise<PageResponse<CatalogEntry>> {
  return listItems(params)
}
</script>

<template>
  <CatalogBrowser
    kind="item"
    :title="t('catalog.items')"
    :fetcher="fetchItems"
  />
</template>
