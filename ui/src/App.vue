<script setup lang="ts">
import { onMounted } from 'vue'
import { useDatasetStore } from '@/stores/dataset'

const store = useDatasetStore()
onMounted(() => store.load())
</script>

<template>
  <router-view />
</template>
