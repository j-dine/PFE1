﻿<script setup lang="ts">
import { computed } from 'vue'
import { ref } from 'vue'
import { useAppStore } from '../stores/appStore'
import { WorkflowStatuts } from '../constants/workflow'

const store = useAppStore()

const activeView = computed({
  get: () => store.activeView,
  set: (val) => { store.activeView = val }
})
const wfSteps = computed(() => store.wfSteps)
// En mode Camunda, l'agent financier travaille sur les dossiers "VALIDE" (tâche Paiement).
const dossiers = computed(() => store.dossiers)
const dossiersARegler = computed(() => dossiers.value.filter((d: any) => String(d?.statutRaw || '').toUpperCase() === 'VALIDE'))
const afStats = computed(() => store.afStats)
const todayISO = computed(() => store.todayISO)
const paymentRef = ref('')
const paymentAmount = ref('')
const paymentNote = ref('')

const openModal = (name: string, data?: any) => store.openModal(name, data)
const addToast = (type: string, msg: string) => store.addToast(type, msg)

const openDocs = (p: any) => {
  const id = p?.id || p?.dossierId || store.dossiers.find((d: any) => d.numero === p?.ref)?.id
  if (!id) {
    addToast('error', 'Dossier introuvable pour consulter les documents')
    return
  }
  store.openArchiveDetails(id)
}
const registerPayment = async () => {
  const target = store.dossiers.find((d: any) => d.numero === paymentRef.value || String(d.id) === String(paymentRef.value)) || store.selectedDossier
  if (!target?.id) {
    addToast('error', 'Reference dossier introuvable')
    return
  }
  try {
    const amount = Number(String(paymentAmount.value || '').replace(',', '.'))
    await store.completePaiementWorkflow(
      target.id,
      amount,
      paymentNote.value || `Paiement enregistré (${paymentAmount.value || 'montant non précisé'})`
    )
    addToast('success', 'Paiement enregistré (workflow)')
    activeView.value = 'af-dashboard'
    paymentRef.value = ''
    paymentAmount.value = ''
    paymentNote.value = ''
  } catch {
    addToast('error', 'Paiement non enregistré côté backend')
  }
}
</script>

<template>
  <div class="topbar">
    <div class="topbar-title">
      <span v-if="activeView==='af-dashboard'">Gestion des paiements</span>
      <span v-if="activeView==='af-enregistrer'">Enregistrer paiement</span>
      <span v-if="activeView==='af-justificatif'">Générer justificatif</span>
    </div>
    <div class="topbar-actions">
      <button class="btn btn-primary btn-sm" @click="openModal('paiement')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
        Enregistrer paiement
      </button>
    </div>
  </div>
  <div class="content">
    <template v-if="activeView==='af-dashboard'">
      <div class="stats stats-4">
        <div class="stat-card sc-green"><div class="stat-label">Budget disponible</div><div class="stat-value" style="font-size:22px">Auto</div><div class="stat-delta">DT - 2026</div></div>
        <div class="stat-card sc-amber"><div class="stat-label">Paiements en attente</div><div class="stat-value">{{afStats.enAttente}}</div><div class="stat-delta">Nécessite action</div></div>
        <div class="stat-card sc-blue"><div class="stat-label">Payés</div><div class="stat-value">{{afStats.payesMois}}</div><div class="stat-delta">Total</div></div>
        <div class="stat-card sc-red"><div class="stat-label">Dossiers "Payé"</div><div class="stat-value">{{afStats.dossiersPayes}}</div><div class="stat-delta">Clôturés</div></div>
      </div>
      <div class="workflow-strip">
        <div v-for="(s,i) in wfSteps" :key="i" class="wf-step" :class="{done:i<4,active:i===4}">
          <div class="wf-circle"><span v-if="i<4">✓</span><span v-else>{{i+1}}</span></div>
          <div class="wf-label">{{s.label}}</div>
        </div>
      </div>
      <div class="col-left-right">
        <div>
          <div class="card">
            <div class="card-head">
              <div><div class="card-title">Dossiers validés - En attente de paiement</div><div class="card-sub">Transmission service financier</div></div>
            </div>
            <table class="tbl">
              <thead><tr><th>Référence</th><th>Objet</th><th>Montant</th><th>Statut paiement</th><th>Échéance</th><th>Actions</th></tr></thead>
              <tbody>
                <tr v-for="d in dossiersARegler" :key="d.id">
                  <td style="font-size:11px;font-weight:800;color:var(--blue)">{{d.numero}}</td>
                  <td>
                    <div style="font-weight:600;font-size:12px">{{d.objet}}</div>
                    <div style="font-size:10px;color:var(--muted)">{{d.expediteur}}</div>
                  </td>
                  <td style="font-weight:700;font-size:12px;color:var(--muted)">À saisir</td>
                  <td><span class="badge b-traitement">En attente</span></td>
                  <td style="font-size:11px;color:var(--muted)">-</td>
                  <td>
                    <div style="display:flex;gap:5px">
                      <button class="btn btn-success btn-sm" @click="paymentRef=String(d.numero||d.id); activeView='af-enregistrer'">Saisir</button>
                      <button class="btn btn-outline btn-sm" @click="openDocs(d)">Docs</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div>
          <div class="card">
            <div class="card-head"><div class="card-title">Saisie paiement</div></div>
            <div style="padding:16px">
              <div class="form-grid" style="gap:12px">
                <div class="form-group"><label class="form-label">Réf. dossier</label><input class="form-input" placeholder="CO-2026-XXXX" v-model="paymentRef"></div>
                <div class="form-group"><label class="form-label">Montant (DT)</label><input type="number" class="form-input" placeholder="0.00" v-model="paymentAmount"></div>
                <div class="form-group"><label class="form-label">Date paiement</label><input type="date" class="form-input" :value="todayISO"></div>
                <div class="form-group"><label class="form-label">Mode de paiement</label>
                  <select class="form-select"><option>Virement bancaire</option><option>Chèque</option><option>Caisse</option></select></div>
                <div class="form-group"><label class="form-label">Référence bancaire</label><input class="form-input" placeholder="N° ordre de virement..." v-model="paymentNote"></div>
              </div>
              <div style="margin-top:14px;display:flex;flex-direction:column;gap:8px">
                <button class="btn btn-success" style="width:100%;justify-content:center" @click="registerPayment">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                  Enregistrer & changer statut
                </button>
                <button class="btn btn-outline" style="width:100%;justify-content:center" @click="activeView='af-justificatif';addToast('info','Génération du justificatif...')">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/></svg>
                  Générer justificatif
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-if="activeView==='af-enregistrer'">
      <div class="card">
        <div class="card-head"><div class="card-title">Enregistrer paiement</div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group"><label class="form-label">Réf. dossier</label><input class="form-input" placeholder="CO-2026-XXXX" v-model="paymentRef"></div>
            <div class="form-group"><label class="form-label">Date paiement</label><input type="date" class="form-input" :value="todayISO"></div>
            <div class="form-group"><label class="form-label">Montant (DT)</label><input type="number" class="form-input" placeholder="0.00" v-model="paymentAmount"></div>
            <div class="form-group"><label class="form-label">Mode de paiement</label>
              <select class="form-select"><option>Virement bancaire</option><option>Chèque</option><option>Caisse</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Référence bancaire</label><input class="form-input" placeholder="N° ordre de virement / chèque..." v-model="paymentNote"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Remarques</label><textarea class="form-textarea" placeholder="..." v-model="paymentNote"></textarea></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
            <button class="btn btn-outline" @click="activeView='af-dashboard'">Annuler</button>
            <button class="btn btn-success" @click="registerPayment">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              Confirmer le paiement
            </button>
          </div>
        </div>
      </div>
    </template>

    <template v-if="activeView==='af-justificatif'">
      <div class="card">
        <div class="card-head"><div><div class="card-title">Générer justificatif</div><div class="card-sub">Edition et export du reçu de paiement</div></div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group"><label class="form-label">Réf. dossier</label><input class="form-input" placeholder="CO-2026-XXXX"></div>
            <div class="form-group"><label class="form-label">Format</label>
              <select class="form-select"><option>PDF</option><option>Word</option></select></div>
            <div class="form-group"><label class="form-label">Signataire</label><input class="form-input" placeholder="Nom du signataire"></div>
            <div class="form-group"><label class="form-label">Date d'édition</label><input type="date" class="form-input" :value="todayISO"></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px;margin-top:20px;padding-top:16px;border-top:1px solid var(--border)">
            <button class="btn btn-outline" @click="activeView='af-dashboard'">Retour</button>
            <button class="btn btn-primary" @click="addToast('success','Justificatif généré avec succès !')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              Générer & Télécharger
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
