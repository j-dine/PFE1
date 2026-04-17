<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '../stores/appStore'

const store = useAppStore()

const activeView = computed({
  get: () => store.activeView,
  set: (val) => { store.activeView = val }
})
const wfSteps = computed(() => store.wfSteps)
const dossiers = computed(() => store.dossiers)
const decisionsResp = computed(() => store.decisionsResp)
const respStats = computed(() => store.respStats)
const todayISO = computed(() => store.todayISO)

const openModal = (name: string, data?: any) => store.openModal(name, data)
const addToast = (type: string, msg: string) => store.addToast(type, msg)
const openDocs = (d: any) => {
  if (d?.id) store.openArchiveDetails(d.id)
}
</script>

<template>
  <div class="topbar">
    <div class="topbar-title">
      <span v-if="activeView==='resp-dashboard'">File de validation</span>
      <span v-if="activeView==='resp-rapport'">Générer rapport</span>
    </div>
    <div class="topbar-actions">
      <button class="btn btn-outline btn-sm" @click="activeView='resp-rapport'">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
        Générer rapport
      </button>
    </div>
  </div>
  <div class="content">
    <template v-if="activeView==='resp-dashboard'">
      <div class="stats stats-5">
        <div class="stat-card sc-amber"><div class="stat-label">En attente validation</div><div class="stat-value">{{respStats.enAttente}}</div></div>
        <div class="stat-card sc-green"><div class="stat-label">Validés</div><div class="stat-value">{{respStats.validesMois}}</div><div class="stat-delta">Taux {{respStats.taux}}%</div></div>
        <div class="stat-card sc-red"><div class="stat-label">Rejetés</div><div class="stat-value">{{respStats.rejetes}}</div><div class="stat-delta">Action requise</div></div>
        <div class="stat-card sc-purple"><div class="stat-label">Avec commentaire</div><div class="stat-value">{{respStats.avecCommentaire}}</div></div>
        <div class="stat-card sc-blue"><div class="stat-label">Délai moyen</div><div class="stat-value" style="font-size:22px">Auto</div></div>
      </div>
      <div class="workflow-strip">
        <div v-for="(s,i) in wfSteps" :key="i" class="wf-step" :class="{done:i<3,active:i===3}">
          <div class="wf-circle"><span v-if="i<3">✓</span><span v-else>{{i+1}}</span></div>
          <div class="wf-label">{{s.label}}</div>
        </div>
      </div>
      <div class="card">
        <div class="card-head">
          <div><div class="card-title">Dossiers soumis à validation</div><div class="card-sub">Décision requise - Traçabilité assurée</div></div>
          <div style="display:flex;gap:8px">
            <select class="form-select" style="width:130px;font-size:11px;padding:6px 10px"><option>Tous</option><option>Urgent</option><option>Normal</option></select>
          </div>
        </div>
        <table class="tbl">
          <thead><tr><th>Référence</th><th>Objet</th><th>Agent traitant</th><th>Priorité</th><th>Délai</th><th>Décision</th></tr></thead>
          <tbody>
            <tr v-for="d in dossiers.filter((x:any)=>x.statutKey==='validation'||x.statutKey==='traitement')" :key="d.id">
              <td style="font-size:11px;font-weight:800;color:var(--blue)">{{d.numero}}</td>
              <td>
                <div style="font-weight:600;font-size:12px">{{d.objet}}</div>
                <div style="font-size:10px;color:var(--muted)">{{d.expediteur}}</div>
              </td>
              <td>
                <div style="display:flex;align-items:center;gap:8px">
                  <div class="user-av-sm" :style="{background:d.agentColor||'var(--blue)'}">{{d.agentInit||'AG'}}</div>
                  <span style="font-size:11px;font-weight:600">{{d.agent||'Agent'}}</span>
                </div>
              </td>
              <td><span class="badge" :class="d.priorite==='Urgent'?'b-urgent':'b-normal'">{{d.priorite}}</span></td>
              <td style="font-size:11px" :style="{color:d.urgent?'var(--red)':'var(--muted)'}">{{d.deadline}}</td>
              <td>
                <div style="display:flex;gap:5px">
                  <button class="btn btn-success btn-sm" @click="openModal('valider',d)">✓ Valider</button>
                  <button class="btn btn-soft-danger btn-sm" @click="openModal('rejeter',d)">✗ Rejeter</button>
                  <button class="btn btn-soft-purple btn-sm" @click="openModal('commenter',d)">💬 Commenter</button>
                  <button class='btn btn-outline btn-sm' @click='openDocs(d)'>Docs</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <!-- RECENT DECISIONS -->
      <div class="card">
        <div class="card-head"><div class="card-title">Décisions récentes</div></div>
        <table class="tbl">
          <thead><tr><th>Référence</th><th>Décision</th><th>Commentaire</th><th>Date</th></tr></thead>
          <tbody>
            <tr v-for="d in decisionsResp" :key="d.id">
              <td style="font-size:11px;font-weight:800;color:var(--blue)">{{d.ref}}</td>
              <td><span class="badge" :class="String(d.decision||'').toLowerCase().includes('valid')?'b-paiement':'b-rejete'">{{d.decision}}</span></td>
              <td style="font-size:11px;color:var(--muted)">{{d.comment}}</td>
              <td style="font-size:11px;color:var(--muted)">{{d.date}}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <template v-if="activeView==='resp-rapport'">
      <div class="card">
        <div class="card-head"><div class="card-title">Générateur de rapport</div></div>
        <div style="padding:22px">
          <div class="form-grid form-2" style="gap:16px;margin-bottom:20px">
            <div class="form-group"><label class="form-label">Période - Du</label><input type="date" class="form-input" value="2026-03-01"></div>
            <div class="form-group"><label class="form-label">Période - Au</label><input type="date" class="form-input" :value="todayISO"></div>
            <div class="form-group"><label class="form-label">Type de rapport</label>
              <select class="form-select"><option>Rapport d'activité mensuel</option><option>Rapport de validation</option><option>Rapport de traitement</option><option>Statistiques générales</option></select></div>
            <div class="form-group"><label class="form-label">Format d'export</label>
              <select class="form-select"><option>PDF</option><option>Excel</option><option>Word</option></select></div>
          </div>
          <div style="display:flex;justify-content:flex-end;gap:10px">
            <button class="btn btn-primary" @click="addToast('success','Rapport généré et téléchargé !')">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
              Générer & Télécharger
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
