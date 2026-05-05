﻿      <script setup lang="ts">
      import { computed, watch } from 'vue'
      import { ref } from 'vue'
      import { useAppStore } from '../stores/appStore'
      import { WorkflowStatuts } from '../constants/workflow'
      import DocumentsSection from '../components/DocumentsSection.vue'

      const store = useAppStore()

      const activeView = computed(() => store.activeView)
      const wfSteps = computed(() => store.wfSteps)
      const dossiers = computed(() => store.dossiers)
      const tasks = computed(() => store.workflowTasks || [])
      const responseForm = ref({
        to: '',
        type: 'Réponse officielle',
        subject: '',
        body: '',
        method: 'Bureau d\'Ordre'
      })

      const traitementDossiers = computed(() => {
        const map = new Map((dossiers.value || []).map((d: any) => [String(d.id), d]))
        return (tasks.value || [])
          .filter((t: any) => String(t?.taskDefinitionKey || '') === 'UserTask_Traitement')
          .map((t: any) => map.get(String(t.dossierId)))
          .filter(Boolean)
      })
      const selectedDossier = computed(() => store.selectedDossier)
      const selectedDocs = computed(() => store.selectedDossierDocuments || [])
      const asStats = computed(() => store.asStats)
      const traitementNote = ref('')
      const selectedDocsLoading = computed(() => store.selectedDossierDocsLoading)

      // Auto-chargement des documents
      watch(selectedDossier, (newD) => {
        if (newD?.id) {
          store.loadSelectedDossierDocuments(newD.id)
        }
      }, { immediate: true })

      const selectDossier = (d: any) => store.selectDossier(d)
      const openModal = (name: string, data?: any) => store.openModal(name, data)
      const addToast = (type: string, msg: string) => store.addToast(type, msg)

const openDocs = () => {
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
  } catch (err: any) {
    addToast('error', err?.message || 'Impossible d\'ouvrir le document (backend/stockage)')
  }
}

      const sendResponseEmail = async () => {
        if (!responseForm.value.to || !responseForm.value.subject || !responseForm.value.body) {
          addToast('error', 'Veuillez remplir tous les champs de l\'e-mail')
          return
        }
        await store.sendEmailResponse({
          to: responseForm.value.to,
          subject: responseForm.value.subject,
          body: responseForm.value.body
        })
      }

const transmitToValidation = async () => {
  if (!selectedDossier.value?.id) {
    addToast('error', 'Sélectionnez un dossier')
    return
  }
        try {
          await store.transitionDossier(
            selectedDossier.value.id,
            WorkflowStatuts.EN_COURS,
            traitementNote.value || 'Transmis en validation'
          )
          addToast('success', 'Dossier transmis au responsable pour validation')
          traitementNote.value = ''
        } catch {
          addToast('error', 'Transition impossible coté backend')
        }
      }

      const resubmitDossier = async () => {
        if (!selectedDossier.value?.id) return
        try {
          await store.transitionDossier(
            selectedDossier.value.id,
            WorkflowStatuts.EN_COURS,
            traitementNote.value || 'Dossier corrigé et retransmis'
          )
          addToast('success', 'Dossier retransmis avec succès')
          traitementNote.value = ''
        } catch {
          addToast('error', 'Erreur lors de la retransmission')
        }
      }
      </script>

      <template>
        <div class="topbar">
          <div class="topbar-title">
            <span v-if="activeView==='as-dashboard'">Mes dossiers assignés</span>
            <span v-if="activeView==='as-traiter'">Traitement des dossiers</span>
            <span v-if="activeView==='as-repondre'">Rédiger une réponse</span>
          </div>
          <div class="topbar-actions">
            <button class="btn btn-primary btn-sm" @click="openModal('reponse')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              Rédiger réponse
            </button>
          </div>
        </div>
        <div class="content">
          <template v-if="activeView==='as-dashboard'||activeView==='as-traiter'">
            <div class="stats stats-4" style="margin-bottom:18px">
              <div class="stat-card sc-blue"><div class="stat-label">Reçus</div><div class="stat-value">{{asStats.recus}}</div><div class="stat-delta">Assignés</div></div>
              <div class="stat-card sc-amber"><div class="stat-label">En cours</div><div class="stat-value">{{asStats.enCours}}</div><div class="stat-delta">En traitement</div></div>
              <div class="stat-card sc-green"><div class="stat-label">Transmis</div><div class="stat-value">{{asStats.transmis}}</div><div class="stat-delta">à valider</div></div>
              <div class="stat-card sc-red"><div class="stat-label">Urgents</div><div class="stat-value">{{asStats.urgents}}</div><div class="stat-delta">Action requise</div></div>
            </div>
            <div class="workflow-strip">
              <div v-for="(s,i) in wfSteps" :key="i" class="wf-step" :class="{done:i<2,active:i===2}">
                <div class="wf-circle"><span v-if="i<2">✓</span><span v-else>{{i+1}}</span></div>
                <div class="wf-label">{{s.label}}</div>
              </div>
            </div>
            <div class="col-list-detail">
              <div>
                <div v-for="d in traitementDossiers" :key="d.id"
                  class="dossier-card" :class="{sel:selectedDossier&&selectedDossier.id===d.id}" @click="selectDossier(d)">
                  <div class="dc-top"><span class="dc-ref">{{d.numero}}</span><span class="dc-date">{{d.dateReception}}</span></div>
                  <div class="dc-obj">{{d.objet}}</div>
                  <div class="dc-from">De : {{d.expediteur}}</div>
                  <div class="dc-footer">
                    <span class="badge" :class="'b-'+d.statutKey">{{d.statut}}</span>
                    <span class="badge b-urgent" v-if="d.priorite==='Urgent'">URGENT</span>
                    <div style="display:flex;gap:3px;margin-left:auto">
                      <div v-for="n in 3" :key="n" style="width:8px;height:8px;border-radius:50%;background:var(--border)" :style="{background:n<=d.prioriteLevel?'var(--amber)':'var(--border)'}"></div>
                    </div>
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
                    <div class="dp-row"><span class="dp-key">Priorité</span><span class="dp-val">{{selectedDossier.priorite}}</span></div>
                    <div class="dp-row"><span class="dp-key">Statut actuel</span><span class="dp-val"><span class="badge" :class="'b-'+selectedDossier.statutKey">{{selectedDossier.statut}}</span></span></div>
                    <div class="divider"></div>
                    <div class="form-group"><label class="form-label">Note de traitement</label><textarea class="form-textarea" placeholder="Vos observations..." v-model="traitementNote"></textarea></div>
                    <div class="form-group" style="margin-top:10px"><label class="form-label">Mettre à jour les infos</label><input type="text" class="form-input" placeholder="Information complémentaire..."></div>
                  </div>
                  <div class="dp-actions">
                    <button class="btn btn-outline" style="width:100%;justify-content:center" @click="openDocs">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                      Voir documents
                    </button>
                    <button v-if="selectedDossier.statutKey !== 'rejete'" class="btn btn-success" style="width:100%;justify-content:center" @click="transmitToValidation">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 2 15 22 11 13 2 9 22 2"/></svg>
                      Transmettre à validation
                    </button>
                    <button v-else class="btn btn-primary" style="width:100%;justify-content:center" @click="resubmitDossier">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg>
                      Transmettre à nouveau
                    </button>
                    <button class="btn btn-outline" style="width:100%;justify-content:center" @click="openModal('reponse')">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
                      Rédiger réponse
                    </button>
                    <button class="btn btn-soft-amber" style="width:100%;justify-content:center">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      Mettre à jour infos
                    </button>
                  </div>
                  <DocumentsSection
                    title="Documents"
                    :documents="selectedDocs"
                    :loading="selectedDocsLoading"
                    :can-delete="false"
                    @open="openDocInline"
                  >
                    <template #actions>
                      <button class="btn btn-outline btn-sm" type="button" @click="refreshSelectedDocs">Actualiser</button>
                      <button class="btn btn-outline btn-sm" type="button" @click="openDocs">Voir (modal)</button>
                    </template>
                  </DocumentsSection>
                  <div style="border-top:1px solid var(--border);padding:14px 18px 4px">
                    <div style="font-size:11px;font-weight:700;margin-bottom:8px">Historique</div>
                    <div class="tl-item" v-for="h in selectedDossier.historique" :key="h.id">
                      <div class="tl-dot" :style="{background:h.bg,color:h.color}"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" v-html="h.icon" style="width:11px;height:11px"></svg></div>
                      <div><div class="tl-title">{{h.action}}</div><div class="tl-desc">{{h.user}}</div><div class="tl-time">{{h.date}}</div></div>
                    </div>
                  </div>
                </div>
                <div class="empty-state" v-else>Sélectionnez un dossier</div>
              </div>
            </div>
          </template>
          <template v-if="activeView==='as-repondre'">
            <div class="card">
              <div class="card-head">
                <div>
                  <div class="card-title">Rédaction de réponse</div>
                  <div class="card-sub">Préparez une réponse officielle et transmettez-la</div>
                </div>
              </div>
              <div style="padding:22px">
                <div class="form-grid form-2" style="gap:12px">
                  <div class="form-group"><label class="form-label">Destinataire (Email)</label><input class="form-input" placeholder="exemple@client.com" v-model="responseForm.to"></div>
                  <div class="form-group"><label class="form-label">Type de réponse</label>
                    <select class="form-select" v-model="responseForm.type"><option>Réponse officielle</option><option>Accusé de réception</option><option>Demande de complément</option></select></div>
                  <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet</label><input class="form-input" placeholder="En réponse à..." v-model="responseForm.subject"></div>
                  <div class="form-group" style="grid-column:1/-1"><label class="form-label">Corps du message</label><textarea class="form-textarea" style="min-height:120px" placeholder="Rédigez votre réponse..." v-model="responseForm.body"></textarea></div>
                  <div class="form-group"><label class="form-label">Transmission via</label>
                    <select class="form-select" v-model="responseForm.method"><option>Bureau d'Ordre</option><option>Email direct</option><option>Courrier postal</option></select></div>
                  <div class="form-group"><label class="form-label">Joindre document</label><input type="file" class="form-input" style="padding:6px"></div>
                </div>
                <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
                  <button class="btn btn-outline">Brouillon</button>
                  <button class="btn btn-primary" @click="sendResponseEmail">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
                    Envoyer
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </template>
