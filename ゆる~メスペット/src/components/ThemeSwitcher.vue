<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useTheme, type ThemeMode } from '@/composables/useTheme'

const { getMode, setMode, listenSystem } = useTheme()
const mode = ref<ThemeMode>('auto')
let unlisten: (() => void) | undefined

onMounted(() => {
  mode.value = getMode()
  unlisten = listenSystem()
})

onBeforeUnmount(() => {
  unlisten?.()
})

function change(m: ThemeMode) {
  mode.value = m
  setMode(m)
}
</script>

<template>
  <!-- one-button toggle -->
  <button
    class="btn btn-secondary"
    style="--bs-btn-padding-y: 0.25rem; --bs-btn-padding-x: 0.5rem; --bs-btn-font-size: 0.75rem"
    @click="change(mode === 'dark' ? 'light' : 'dark')"
  >
    🌓 テーマ切替（ライト／ダーク）
  </button>
</template>
