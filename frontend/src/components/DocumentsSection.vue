<script setup lang="ts">
import { computed } from 'vue'

type AnyDoc = Record<string, any>

const props = withDefaults(defineProps<{
  title?: string
  documents?: AnyDoc[]
  loading?: boolean
  canDelete?: boolean
  chrome?: boolean
}>(), {
  title: 'Documents',
  documents: () => [],
  loading: false,
  canDelete: false,
  chrome: true,
})

const emit = defineEmits<{
  (e: 'open', doc: AnyDoc): void
  (e: 'delete', doc: AnyDoc): void
}>()

const docs = computed(() => Array.isArray(props.documents) ? props.documents : [])

const docName = (doc: AnyDoc) => doc?.nomFichier || doc?.filename || doc?.name || `Document #${doc?.id ?? ''}`
const docType = (doc: AnyDoc) => doc?.type || '-'
const docDate = (doc: AnyDoc) => {
  const raw = doc?.dateUpload || doc?.createdAt || doc?.date || null
  if (!raw) return '-'
  const s = String(raw)
  // Support both "YYYY-MM-DD..." and "DD/MM/YYYY".
  if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10).split('-').reverse().join('/')
  return s
}
</script>

<template>
  <div :style="chrome ? 'border-top:1px solid var(--border);padding:14px 18px 10px' : ''">
    <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:10px">
      <div style="font-size:11px;font-weight:800">{{ title }}</div>
      <div style="display:flex;gap:6px;flex-wrap:wrap;justify-content:flex-end">
        <slot name="actions" />
      </div>
    </div>

    <div v-if="loading" style="font-size:12px;color:var(--muted);padding:6px 0">
      Chargement...
    </div>

    <div v-else-if="docs.length === 0" class="empty-state" style="padding:14px">
      <slot name="empty">
        Aucun document associé à ce dossier.
      </slot>
    </div>

    <div v-else style="display:flex;flex-direction:column;gap:8px">
      <div
        v-for="doc in docs"
        :key="doc.id"
        style="display:flex;align-items:center;gap:10px;padding:10px 10px;border:1px solid var(--bg);border-radius:10px;background:#faf9f7"
      >
        <div style="min-width:0;flex:1">
          <div style="font-size:12px;font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
            {{ docName(doc) }}
          </div>
          <div style="font-size:10px;color:var(--muted)">
            {{ docType(doc) }} · {{ docDate(doc) }}
          </div>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0">
          <button class="btn btn-outline btn-sm" type="button" @click="emit('open', doc)">Ouvrir</button>
          <button v-if="canDelete" class="btn btn-soft-danger btn-sm" type="button" @click="emit('delete', doc)">Supprimer</button>
        </div>
      </div>
    </div>
  </div>
</template>

