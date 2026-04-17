<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '../stores/appStore'
import AboView from './AboView.vue'
import AsView from './AsView.vue'
import RespView from './RespView.vue'
import AfView from './AfView.vue'
import AdminView from './AdminView.vue'
import Modals from '../components/Modals.vue'
import Toasts from '../components/Toasts.vue'

const store = useAppStore()
const currentRole = computed(() => store.currentRole)
</script>

<template>
  <AboView v-if="currentRole === 'abo'" />
  <AsView v-if="currentRole === 'as'" />
  <RespView v-if="currentRole === 'resp'" />
  <AfView v-if="currentRole === 'af'" />
  <AdminView v-if="currentRole === 'admin'" />

  <Modals />
  <Toasts />
</template>
