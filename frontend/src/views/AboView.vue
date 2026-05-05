<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAppStore } from '../stores/appStore'
import DocumentsSection from '../components/DocumentsSection.vue'

const store = useAppStore()

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFiles = ref<File[]>([])

const dossierForm = ref({
  dateReception: store.todayISO,
  expediteur: '',
  typeDocument: 'Courrier entrant',
  objet: '',
  serviceCible: '',
  priorite: 'NORMALE',
  description: ''
})

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  const incoming = target.files ? Array.from(target.files) : []
  if (!incoming.length) return

  const keyOf = (f: File) => `${f.name}::${f.size}::${f.lastModified}`
  const existing = new Map(selectedFiles.value.map(f => [keyOf(f), f]))
  for (const f of incoming) existing.set(keyOf(f), f)
  selectedFiles.value = Array.from(existing.values())

  // Allow selecting the same file again later (after removing it).
  target.value = ''
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const removeSelectedFile = (idx: number) => {
  if (idx < 0 || idx >= selectedFiles.value.length) return
  selectedFiles.value = selectedFiles.value.filter((_, i) => i !== idx)
}

const formatBytes = (n: number) => {
  const v = Number(n || 0)
  if (!isFinite(v) || v <= 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.min(units.length - 1, Math.floor(Math.log(v) / Math.log(1024)))
  const num = v / Math.pow(1024, i)
  return `${num.toFixed(i === 0 ? 0 : 1)} ${units[i]}`
}

const submitDossier = async () => {
  if (!dossierForm.value.objet || !dossierForm.value.expediteur) {
    addToast('error', 'Veuillez remplir les champs obligatoires (Objet, Expéditeur)')
    return
  }

  try {
    const payload = {
      titre: dossierForm.value.objet || 'Nouveau dossier',
      sujet: dossierForm.value.objet || 'Nouveau dossier',
      description: dossierForm.value.description || '',
      destinataireExterne: dossierForm.value.expediteur || 'Non spécifié',
      dateReception: dossierForm.value.dateReception || new Date().toISOString().split('T')[0],
      serviceCible: dossierForm.value.serviceCible || 'Non assigné',
      priorite: dossierForm.value.priorite || 'NORMALE',
      statut: 'OUVERT',
      locked: false
    }
    
    console.log('Envoi payload dossier:', payload)
    const newDossier = await store.createDossier(payload)

    if (selectedFiles.value.length && newDossier?.id) {
      try {
        await store.uploadDossierDocuments(newDossier.id, selectedFiles.value, 'original')
      } catch (uploadError) {
        console.error('Erreur upload document:', uploadError)
        // "warn" n'existe pas dans le composant Toasts (success/info/error uniquement)
        addToast('info', 'Dossier créé mais certains documents n\'ont pas pu être joints.')
      }
    }

    addToast('success', 'Dossier créé avec succès !')

    // Après création: bascule sur consulter + ouverture automatique de la liste des documents uploadés.
    // Permet de vérifier immédiatement les pièces jointes (ouvrir/supprimer).
    try {
      if (newDossier?.id != null) {
        const found = (store.dossiers || []).find((d: any) => String(d?.id) === String(newDossier.id))
        if (found) store.selectDossier(found)
        activeView.value = 'abo-consulter'
        await store.openArchiveDetails(newDossier.id)
      } else {
        activeView.value = 'abo-dashboard'
      }
    } catch (e) {
      console.warn('open docs after create failed:', e)
      activeView.value = 'abo-consulter'
    }
    
    // Reset form
    dossierForm.value = {
      dateReception: store.todayISO,
      expediteur: '',
      typeDocument: 'Courrier entrant',
      objet: '',
      serviceCible: '',
      priorite: 'NORMALE',
      description: ''
    }
    selectedFiles.value = []
  } catch (error: any) {
    console.error('Erreur détaillée création dossier:', error)
    const msg = error.response?.data?.message || 'Erreur lors de la création du dossier.'
    addToast('error', msg)
  }
}

const activeView = computed({
  get: () => store.activeView,
  set: (val) => { store.activeView = val }
})

const today = computed(() => store.today)
const todayISO = computed(() => store.todayISO)
const wfSteps = computed(() => store.wfSteps)
const dossiers = computed(() => store.dossiers)
const filteredDossiers = computed(() => store.filteredDossiers)
const selectedDossier = computed(() => store.selectedDossier)

// Charge automatiquement les documents dès qu'un dossier est sélectionné
watch(selectedDossier, (newDossier) => {
  if (newDossier?.id) {
    store.loadSelectedDossierDocuments(newDossier.id)
  }
}, { immediate: true })

const activities = computed(() => store.activities)
const archives = computed(() => store.archives)
const apiError = computed(() => store.apiError)
const isLoadingDossiers = computed(() => store.isLoadingDossiers)
const aboStats = computed(() => store.aboStats)
const selectedDocsLoading = computed(() => store.selectedDossierDocsLoading)
const selectedDocs = computed(() => store.selectedDossierDocuments || [])
const currentRole = computed(() => store.currentRole)

const canDeleteDocs = computed(() => {
  // Only BO and Admin can delete documents. Also block deletes on archived/locked dossiers.
  if (currentRole.value !== 'abo' && currentRole.value !== 'admin') return false
  const locked = !!(selectedDossier.value && (selectedDossier.value.locked || String(selectedDossier.value.statutRaw || '').toUpperCase() === 'ARCHIVE'))
  return !locked
})

const dossiersDuJour = computed(() => {
  return store.dossiers.filter((d: any) => d.dateReception === store.todayISO || d.dateReception === new Date().toLocaleDateString('fr-FR'))
})

const searchQuery = computed({
  get: () => store.searchQuery,
  set: (val) => { store.searchQuery = val }
})

const selectedPriority = computed({
  get: () => store.selectedPriority,
  set: (val) => { store.selectedPriority = val }
})

const selectDossier = (d: any) => store.selectDossier(d)
const openModal = (name: string, data?: any) => store.openModal(name, data)
const addToast = (type: string, msg: string) => store.addToast(type, msg)
const refreshDossiers = () => store.fetchDossiers()
const openArchiveDetails = (a: any) => store.openArchiveDetails(a.id)
const openDocsSelected = () => {
  if (selectedDossier.value?.id) store.openArchiveDetails(selectedDossier.value.id)
}
const refreshSelectedDocs = async () => {
  if (!selectedDossier.value?.id) return
  try {
    await store.loadSelectedDossierDocuments(selectedDossier.value.id)
  } catch (e) {
    console.warn('refreshSelectedDocs failed:', e)
  }
}
const openDocInline = async (doc: any) => {
  try {
    await store.openDocument(doc)
  } catch (e: any) {
    addToast('error', e?.message || 'Impossible d\'ouvrir le document')
  }
}
const deleteDocInline = async (doc: any) => {
  const ok = typeof window === 'undefined' ? true : window.confirm('Supprimer ce document ? Cette action est irreversible.')
  if (!ok) return
  try {
    await store.deleteDocument(doc)
    addToast('success', 'Document supprimé.')
  } catch (e: any) {
    addToast('error', e?.message || 'Suppression impossible côté backend')
  }
}

const completeEnregistrement = async () => {
  if (!selectedDossier.value?.id) {
    addToast('error', 'Sélectionnez un dossier')
    return
  }
  try {
    await store.completeEnregistrementWorkflow(selectedDossier.value.id)
    addToast('success', 'Enregistrement validé (workflow)')
  } catch (e: any) {
    addToast('error', e?.message || 'Enregistrement impossible côté workflow')
  }
}
</script>

<style scoped>
.search-container {
  margin-top: 25px !important;
  margin-bottom: 25px !important;
  background: white;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}
</style>

<template>
  <!-- TOPBAR -->
  <div class="topbar">
    <div class="topbar-title">
      <span v-if="activeView==='abo-dashboard'">Tableau de bord</span>
      <span v-if="activeView==='abo-creer'">Créer un dossier</span>
      <span v-if="activeView==='abo-enregistrer'">Enregistrer courrier</span>
      <span v-if="activeView==='abo-consulter'">Consulter dossiers</span>
      <span v-if="activeView==='abo-archive'">Consulter archives</span>
      <span v-if="activeView==='abo-uploader'">Uploader document</span>
    </div>
    <div class="topbar-actions">
      <span style="font-size:11px;color:var(--muted)">{{ today }}</span>
      <button class="btn btn-outline btn-sm notif-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
        <div class="notif-dot"></div>
      </button>
      <button class="btn btn-primary btn-sm" @click="openModal('creerDossier')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        Nouveau dossier
      </button>
    </div>
  </div>

  <div class="content">
    <div v-if="apiError" style="background:var(--red-s);border:1px solid #fecaca;border-left:4px solid var(--red);border-radius:10px;padding:10px 14px;margin-bottom:14px;font-size:12px;color:var(--red)">
      Backend indisponible: {{ apiError }}
      <button class="btn btn-outline btn-sm" style="margin-left:10px" @click="refreshDossiers">Réessayer</button>
    </div>
    <!-- DASHBOARD ABO -->
    <template v-if="activeView==='abo-dashboard'">
      <div class="stats stats-4">
        <div class="stat-card sc-blue"><div class="stat-label">Dossiers créés</div><div class="stat-value">{{aboStats.crees}}</div><div class="stat-delta">Total base</div></div>
        <div class="stat-card sc-amber"><div class="stat-label">En traitement</div><div class="stat-value">{{aboStats.enTraitement}}</div><div class="stat-delta">Assignés AS</div></div>
        <div class="stat-card sc-green"><div class="stat-label">Archivés</div><div class="stat-value">{{aboStats.archives}}</div><div class="stat-delta">Clôturés</div></div>
        <div class="stat-card sc-red"><div class="stat-label">Urgents</div><div class="stat-value">{{aboStats.urgents}}</div><div class="stat-delta">Action requise</div></div>
      </div>
      <!-- WORKFLOW -->
      <div class="workflow-strip">
        <div class="wf-step done" v-for="(s,i) in wfSteps" :key="i" :class="{done:s.done,active:s.active}">
          <div class="wf-circle"><span v-if="s.done">✓</span><span v-else>{{i+1}}</span></div>
          <div class="wf-label">{{s.label}}</div>
        </div>
      </div>
      <div class="col-left-right">
        <div>
          <div class="card">
            <div class="card-head">
              <div><div class="card-title">Dossiers récents</div><div class="card-sub">Dernières 24h</div></div>
              <button class="btn btn-outline btn-sm" @click="activeView='abo-consulter'">Voir tout</button>
            </div>
            <table class="tbl">
              <thead><tr><th>Référence</th><th>Objet</th><th>Expéditeur</th><th>Statut</th><th>Actions</th></tr></thead>
              <tbody>
                    <tr v-if="isLoadingDossiers"><td colspan="5" style="color:var(--muted)">Chargement des dossiers...</td></tr>
                <tr v-for="d in dossiers.slice(0,5)" :key="d.id">
                  <td style="font-size:11px;font-weight:800;color:var(--blue)">{{d.numero}}</td>
                  <td><div style="font-weight:600;font-size:12px">{{d.objet}}</div><div style="font-size:10px;color:var(--muted)">{{d.expediteur}}</div></td>
                  <td style="font-size:11px;color:var(--muted)">{{d.expediteur}}</td>
                  <td><span class="badge" :class="'b-'+d.statutKey">{{d.statut}}</span></td>
                  <td><button class="btn btn-outline btn-sm" @click="selectDossier(d);activeView='abo-consulter'">Voir</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div>
          <div class="card" style="margin-bottom:14px">
            <div class="card-head"><div class="card-title">Enregistrement rapide</div></div>
            <div style="padding:16px">
              <div class="form-grid">
                <div class="form-group"><label class="form-label">Type courrier</label>
                  <select class="form-select"><option>Entrant</option><option>Sortant</option><option>Interne</option></select></div>
                <div class="form-group"><label class="form-label">Date réception</label>
                  <input type="date" class="form-input" :value="todayISO"></div>
                <div class="form-group" style="grid-column:1/-1"><label class="form-label">Expéditeur</label>
                  <input type="text" class="form-input" placeholder="Nom ou organisme..."></div>
                <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet</label>
                  <input type="text" class="form-input" placeholder="Objet du courrier..."></div>
                <div class="form-group" style="grid-column:1/-1"><label class="form-label">Priorité</label>
                  <div style="display:flex;gap:6px">
                    <div v-for="p in ['Normal','Urgent','Très urgent']" :key="p" @click="selectedPriority=p"
                      :style="{background:selectedPriority===p?'var(--blue-s)':'var(--bg)',color:selectedPriority===p?'var(--blue)':'var(--muted)',border:'1.5px solid',borderColor:selectedPriority===p?'var(--blue)':'var(--border)',padding:'6px 12px',borderRadius:'7px',cursor:'pointer',fontSize:'11px',fontWeight:'600',transition:'all .15s'}">
                      {{p}}</div>
                  </div>
                </div>
              </div>
              <div style="margin-top:14px">
                <button class="btn btn-primary" style="width:100%;justify-content:center" @click="addToast('success','Courrier enregistré avec succès !')">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                  Enregistrer
                </button>
              </div>

              <div v-if="false && selectedFiles.length" class="tag-row" style="margin-top:10px">
                <div
                  v-for="(f, idx) in selectedFiles"
                  :key="f.name + ':' + f.size + ':' + f.lastModified"
                  class="chip"
                  style="background:var(--bg);color:var(--ink);border:1px solid var(--border);padding:6px 10px;border-radius:8px;gap:8px"
                >
                  <span style="font-weight:700">{{f.name}}</span>
                  <span style="color:var(--muted);font-weight:700">{{formatBytes(f.size)}}</span>
                  <button
                    type="button"
                    title="Supprimer ce fichier"
                    aria-label="Supprimer ce fichier"
                    @click.stop="removeSelectedFile(idx)"
                    style="border:none;background:transparent;color:var(--red);font-weight:900;cursor:pointer;line-height:1;padding:0 2px"
                  >×</button>
                </div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="card-head"><div class="card-title">Activité récente</div></div>
            <div style="padding:0 16px 12px">
              <div class="tl-item" v-for="a in activities" :key="a.id">
                <div class="tl-dot" :style="{background:a.bg,color:a.color}">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" v-html="a.icon"></svg>
                </div>
                <div><div class="tl-title">{{a.title}}</div><div class="tl-desc">{{a.desc}}</div><div class="tl-time">{{a.time}}</div></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- CREER DOSSIER ABO -->
    <template v-if="activeView==='abo-creer'">
      <div class="card">
        <div class="card-head"><div><div class="card-title">Création d'un nouveau dossier</div><div class="card-sub">Étape 1 du workflow — Réception</div></div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:16px">
            <div class="form-group">
              <label class="form-label">Numéro de référence</label>
              <input class="form-input" placeholder="Généré automatiquement" disabled style="background:var(--bg);color:var(--muted);cursor:not-allowed">
            </div>
            <div class="form-group"><label class="form-label">Date de réception</label><input type="date" class="form-input" v-model="dossierForm.dateReception"></div>
            <div class="form-group"><label class="form-label">Expéditeur *</label><input type="text" class="form-input" placeholder="Nom / Organisme expéditeur" v-model="dossierForm.expediteur"></div>
            <div class="form-group"><label class="form-label">Type de document</label>
              <select class="form-select" v-model="dossierForm.typeDocument"><option>Courrier entrant</option><option>Courrier sortant</option><option>Note interne</option><option>Demande</option><option>Rapport</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet du dossier *</label><input type="text" class="form-input" placeholder="Décrivez l'objet du dossier..." v-model="dossierForm.objet"></div>
            <div class="form-group"><label class="form-label">Service destinataire</label>
              <select class="form-select" v-model="dossierForm.serviceCible"><option value="">-- Choisir --</option><option>Direction Générale</option><option>Service Financier</option><option>Service Technique</option><option>Service RH</option><option>Service Juridique</option></select></div>
            <div class="form-group"><label class="form-label">Priorité</label>
              <select class="form-select" v-model="dossierForm.priorite"><option value="NORMALE">Normal</option><option value="URGENT">Urgent</option><option value="TRES_URGENT">Très urgent</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Description / Remarques</label><textarea class="form-textarea" placeholder="Informations complémentaires..." v-model="dossierForm.description"></textarea></div>
            <div class="form-group" style="grid-column:1/-1">
              <label class="form-label">Joindre documents (numérisation)</label>
              <input type="file" ref="fileInput" style="display:none" multiple @change="handleFileSelect">
              <div @click="triggerFileInput" style="border:2px dashed var(--border);border-radius:10px;padding:28px;text-align:center;cursor:pointer;transition:all .2s;background:var(--bg)">
                <svg viewBox="0 0 24 24" fill="none" stroke="var(--muted)" stroke-width="1.5" style="width:32px;height:32px;margin:0 auto 8px;display:block"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                <div style="font-size:12px;color:var(--muted)">
                  <span v-if="!selectedFiles.length">Glissez les fichiers ici ou <span style="color:var(--blue);font-weight:600">parcourez</span></span>
                  <span v-else style="color:var(--green);font-weight:700">
                    {{selectedFiles.length}} fichier(s) sélectionné(s): {{selectedFiles.map(f=>f.name).slice(0,3).join(', ')}}<span v-if="selectedFiles.length>3">...</span>
                  </span>
                </div>
                <div style="font-size:10px;color:var(--faint);margin-top:4px">PDF, JPG, PNG — Max 10 Mo</div>
              </div>

              <div v-if="selectedFiles.length" class="tag-row" style="margin-top:10px">
                <div
                  v-for="(f, idx) in selectedFiles"
                  :key="f.name + ':' + f.size + ':' + f.lastModified"
                  class="chip"
                  style="background:var(--bg);color:var(--ink);border:1px solid var(--border);padding:6px 10px;border-radius:8px;gap:8px"
                >
                  <span style="font-weight:700">{{f.name}}</span>
                  <span style="color:var(--muted);font-weight:700">{{formatBytes(f.size)}}</span>
                  <button
                    type="button"
                    title="Supprimer ce fichier"
                    aria-label="Supprimer ce fichier"
                    @click.stop="removeSelectedFile(idx)"
                    style="border:none;background:transparent;color:var(--red);cursor:pointer;line-height:1;padding:0 2px;display:flex;align-items:center"
                  >
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="width:14px;height:14px">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
            <button class="btn btn-outline" @click="activeView='abo-dashboard'">Annuler</button>
            <button class="btn btn-primary" @click="submitDossier">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              Créer & démarrer workflow
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- CONSULTER DOSSIERS ABO -->
    <template v-if="activeView==='abo-consulter'">
      <div class="col-list-detail">
        <div>
          <div class="search-container">
            <input type="text" class="form-input" style="flex:1" placeholder="🔍 Rechercher par référence, objet, expéditeur..." v-model="searchQuery">
            <select class="form-select" style="width:150px">
              <option>Tous statuts</option>
              <option>Réception</option><option>Enregistrement</option><option>Traitement</option><option>Validation</option><option>Paiement</option><option>Archivé</option>
            </select>
          </div>
          <div v-for="d in filteredDossiers" :key="d.id" class="dossier-card" :class="{sel:selectedDossier&&selectedDossier.id===d.id}" @click="selectDossier(d)">
            <div class="dc-top"><span class="dc-ref">{{d.numero}}</span><span class="dc-date">{{d.dateReception}}</span></div>
            <div class="dc-obj">{{d.objet}}</div>
            <div class="dc-from">{{d.expediteur}}</div>
            <div class="dc-footer">
              <span class="badge" :class="'b-'+d.statutKey">{{d.statut}}</span>
              <span class="badge b-urgent" v-if="d.priorite==='Urgent'">URGENT</span>
              <span style="font-size:10px;color:var(--muted);margin-left:auto">{{d.service}}</span>
            </div>
          </div>
        </div>
        <div>
          <div class="detail-panel" v-if="selectedDossier">
            <div class="dp-header">
              <div class="dp-ref">{{selectedDossier.numero}}</div>
              <div class="dp-obj">{{selectedDossier.objet}}</div>
            </div>
            <div class="dp-body">
              <div class="dp-row"><span class="dp-key">Expéditeur</span><span class="dp-val">{{selectedDossier.expediteur}}</span></div>
              <div class="dp-row"><span class="dp-key">Date réception</span><span class="dp-val">{{selectedDossier.dateReception}}</span></div>
              <div class="dp-row"><span class="dp-key">Service</span><span class="dp-val">{{selectedDossier.service}}</span></div>
              <div class="dp-row"><span class="dp-key">Priorité</span><span class="dp-val">{{selectedDossier.priorite}}</span></div>
              <div class="dp-row"><span class="dp-key">Statut</span><span class="dp-val"><span class="badge" :class="'b-'+selectedDossier.statutKey">{{selectedDossier.statut}}</span></span></div>
            </div>
            <div class="dp-actions">
              <button
                class="btn btn-success"
                style="width:100%;justify-content:center"
                v-if="String(selectedDossier.statutRaw||'').toUpperCase()==='RECU' || String(selectedDossier.statutRaw||'').toUpperCase()==='OUVERT'"
                @click="completeEnregistrement"
              >
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                Valider enregistrement
              </button>
              <button class="btn btn-outline" style="width:100%;justify-content:center" @click="openModal('uploadDoc')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                Uploader document
              </button>
              <button class="btn btn-outline" style="width:100%;justify-content:center" @click="openDocsSelected">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                Voir documents
              </button>
              <button class="btn btn-outline" style="width:100%;justify-content:center" @click="openModal('modifierDossier')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Modifier dossier
              </button>
              <button class="btn btn-soft-amber" style="width:100%;justify-content:center" @click="openModal('archiver')" v-if="String(selectedDossier.statutRaw||'').toUpperCase()==='PAYE'">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/></svg>
                Archiver le dossier
              </button>
            </div>

            <DocumentsSection
              title="Documents"
              :documents="selectedDocs"
              :loading="selectedDocsLoading"
              :can-delete="canDeleteDocs"
              @open="openDocInline"
              @delete="deleteDocInline"
            >
              <template #actions>
                <button class="btn btn-outline btn-sm" type="button" @click="refreshSelectedDocs">Actualiser</button>
              </template>
              <template #empty>
                Aucun document associé à ce dossier.
                <div style="margin-top:10px;display:flex;gap:8px;justify-content:center;flex-wrap:wrap">
                  <button class="btn btn-outline btn-sm" type="button" @click="openModal('uploadDoc')">Uploader</button>
                  <button class="btn btn-outline btn-sm" type="button" @click="openDocsSelected">Voir (modal)</button>
                </div>
              </template>
            </DocumentsSection>

            <div style="border-top:1px solid var(--border);padding:14px 18px 4px">
              <div style="font-size:11px;font-weight:700;margin-bottom:10px">Historique des actions</div>
              <div class="tl-item" v-for="h in selectedDossier.historique" :key="h.id">
                <div class="tl-dot" :style="{background:h.bg,color:h.color}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" v-html="h.icon" style="width:11px;height:11px"></svg></div>
                <div><div class="tl-title">{{h.action}}</div><div class="tl-desc">{{h.user}}</div><div class="tl-time">{{h.date}}</div></div>
              </div>
            </div>
          </div>
          <div class="empty-state" v-else>
            <svg viewBox="0 0 24 24" fill="none" stroke="var(--faint)" stroke-width="1.5" style="width:40px;height:40px;margin:0 auto 10px;display:block"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/></svg>
            Sélectionnez un dossier
          </div>
        </div>
      </div>
    </template>

    <!-- ENREGISTRER COURRIER ABO -->
    <template v-if="activeView==='abo-enregistrer'">
      <div class="card">
        <div class="card-head"><div><div class="card-title">Enregistrer courrier</div><div class="card-sub">Réception et enregistrement rapide</div></div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:16px">
            <div class="form-group"><label class="form-label">Type courrier</label>
              <select class="form-select"><option>Entrant</option><option>Sortant</option><option>Interne</option></select></div>
            <div class="form-group"><label class="form-label">Date réception</label><input type="date" class="form-input" :value="todayISO"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Expéditeur</label><input type="text" class="form-input" placeholder="Nom / organisme expéditeur"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet</label><input type="text" class="form-input" placeholder="Objet du courrier..."></div>
            <div class="form-group"><label class="form-label">Service destinataire</label>
              <select class="form-select"><option>Direction Générale</option><option>Service Financier</option><option>Service Technique</option><option>RH</option></select></div>
            <div class="form-group"><label class="form-label">Priorité</label>
              <select class="form-select"><option>Normal</option><option>Urgent</option><option>Très urgent</option></select></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
            <button class="btn btn-outline" @click="activeView='abo-dashboard'">Annuler</button>
            <button class="btn btn-primary" @click="activeView='abo-consulter';addToast('success','Courrier enregistré avec succès !')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              Enregistrer courrier
            </button>
          </div>
        </div>
      </div>

      <!-- FILE D'ATTENTE COURRIERS DU JOUR -->
      <div class="card" style="margin-top: 20px;">
        <div class="card-head">
          <div>
            <div class="card-title">File d'attente — courriers du jour</div>
            <div class="card-sub">Liste des enregistrements effectués aujourd'hui</div>
          </div>
          <span class="badge b-reception">{{ dossiersDuJour.length }} dossier(s)</span>
        </div>
        <table class="tbl">
          <thead>
            <tr>
              <th>Référence</th>
              <th>Objet</th>
              <th>Expéditeur</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!dossiersDuJour.length">
              <td colspan="5" style="text-align: center; padding: 30px; color: var(--muted);">
                Aucun courrier enregistré aujourd'hui.
              </td>
            </tr>
            <tr v-for="d in dossiersDuJour" :key="d.id">
              <td style="font-size:11px;font-weight:800;color:var(--blue)">{{ d.numero }}</td>
              <td><div style="font-weight:600;font-size:12px">{{ d.objet }}</div></td>
              <td style="font-size:11px;color:var(--muted)">{{ d.expediteur }}</td>
              <td><span class="badge" :class="'b-'+d.statutKey">{{ d.statut }}</span></td>
              <td>
                <button class="btn btn-outline btn-sm" @click="selectDossier(d); activeView='abo-consulter'">
                  Détails
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <!-- UPLOADER DOCUMENT ABO -->
    <template v-if="activeView==='abo-uploader'">
      <div class="card">
        <div class="card-head"><div><div class="card-title">Uploader document</div><div class="card-sub">Ajout de pièces dans un dossier</div></div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:16px">
            <div class="form-group"><label class="form-label">Référence dossier</label><input class="form-input" placeholder="CO-2026-XXXX"></div>
            <div class="form-group"><label class="form-label">Type document</label>
              <select class="form-select"><option>Document original</option><option>Copie numérisée</option><option>Annexe</option><option>Justificatif</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Commentaire</label><input class="form-input" placeholder="Description du document..."></div>
            <div class="form-group" style="grid-column:1/-1">
              <label class="form-label">Fichiers</label>
              <div style="border:2px dashed var(--border);border-radius:10px;padding:30px;text-align:center;background:var(--bg)">
                <svg viewBox="0 0 24 24" fill="none" stroke="var(--muted)" stroke-width="1.5" style="width:32px;height:32px;margin:0 auto 8px;display:block"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
                <div style="font-size:12px;color:var(--muted)">Glissez les fichiers ici ou cliquez pour sélectionner</div>
                <div style="font-size:10px;color:var(--faint);margin-top:4px">PDF, JPG, PNG — Max 10 Mo</div>
              </div>
            </div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
            <button class="btn btn-outline" @click="activeView='abo-dashboard'">Annuler</button>
            <button class="btn btn-primary" @click="addToast('success','Document uploadé et stocké dans MinIO !')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              Uploader document
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- ARCHIVE ABO -->
    <template v-if="activeView==='abo-archive'">
      <div class="card">
        <div class="card-head">
          <div><div class="card-title">Archives électroniques</div></div>
          <button class="btn btn-outline btn-sm">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            Recherche avancée
          </button>
        </div>
        <table class="tbl">
          <thead><tr><th>Référence</th><th>Objet</th><th>Date archivage</th><th>Opérateur</th><th>Documents</th><th>Actions</th></tr></thead>
          <tbody>
            <tr v-for="a in archives" :key="a.id">
              <td style="font-size:11px;font-weight:800;color:var(--blue)">{{a.ref}}</td>
              <td><div style="font-weight:600;font-size:12px">{{a.objet}}</div><div style="font-size:10px;color:var(--muted)">{{a.from}}</div></td>
              <td style="font-size:11px;color:var(--muted)">{{a.date}}</td>
              <td style="font-size:11px">{{a.operateur}}</td>
              <td><span class="chip" style="background:var(--blue-s);color:var(--blue)">{{a.docs}} fichier(s)</span></td>
              <td><button class="btn btn-outline btn-sm" @click="openArchiveDetails(a)">Consulter</button></td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </div>
</template>
