<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '../stores/appStore'

const store = useAppStore()
const toasts = computed(() => store.toasts)
</script>

<template>
  <div class="toast-wrap">
    <div v-for="t in toasts" :key="t.id" class="toast" :class="'toast-'+t.type">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
        <polyline v-if="t.type==='success'" points="20 6 9 17 4 12"/>
        <circle v-if="t.type==='info'" cx="12" cy="12" r="10"/><line v-if="t.type==='info'" x1="12" y1="8" x2="12" y2="12"/><line v-if="t.type==='info'" x1="12" y1="16" x2="12.01" y2="16"/>
        <polygon v-if="t.type==='error'" points="7.86 2 16.14 2 22 7.86 22 16.14 16.14 22 7.86 22 2 16.14 2 7.86 7.86 2"/><line v-if="t.type==='error'" x1="15" y1="9" x2="9" y2="15"/><line v-if="t.type==='error'" x1="9" y1="9" x2="15" y2="15"/>
      </svg>
      {{t.msg}}
    </div>
  </div>
</template>
