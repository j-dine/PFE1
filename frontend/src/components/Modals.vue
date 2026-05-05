<script setup lang="ts">
import { computed, watch } from 'vue'
import { ref } from 'vue'
import { useAppStore } from '../stores/appStore'
import { WorkflowStatuts } from '../constants/workflow'
import DocumentsSection from './DocumentsSection.vue'

const store = useAppStore()

const modalOpen = computed({
  get: () => store.modalOpen,
  set: (val) => { store.modalOpen = val }
})
const activeModal = computed(() => store.activeModal)
const modalDossier = computed(() => store.modalDossier)
const modalUser = computed({
  get: () => store.modalUser,
  set: (val) => { store.modalUser = val }
})
const selectedDossier = computed(() => store.selectedDossier)
const todayISO = computed(() => store.todayISO)
const uploadFiles = ref<File[]>([])
const uploadType = ref('Document original')
const uploadComment = ref('')
const decisionComment = ref('')
const commentText = ref('')
const commentVisibility = ref('agent')
const archiveNote = ref('')
const archiveDetailsLoading = computed(() => store.archiveDetailsLoading)
const archiveDetailsDossier = computed(() => store.archiveDetailsDossier)
const archiveDetailsDocuments = computed(() => store.archiveDetailsDocuments)
const currentRole = computed(() => store.currentRole)

const editDossierForm = ref({
  objet: '',
  expediteur: '',
  service: '',
  priorite: '',
  remarques: ''
})

const canDeleteDocs = computed(() => {
  // Only BO and Admin can remove documents (avoid accidental deletes by other roles).
  if (currentRole.value !== 'abo' && currentRole.value !== 'admin') return false
  const locked = !!archiveDetailsDossier.value?.locked
  return !locked
})
const openDoc = async (doc: any) => {
  try {
    await store.openDocument(doc)
  } catch (err: any) {
    const msg = err?.message
    addToast('error', (msg && typeof msg === 'string') ? msg : 'Impossible d\'ouvrir le document (backend/stockage)')
  }
}

const deleteDoc = async (doc: any) => {
  const ok = typeof window === 'undefined' ? true : window.confirm('Supprimer ce document ? Cette action est irreversible.')
  if (!ok) return
  try {
    await store.deleteDocument(doc)
    addToast('success', 'Document supprimé.')
  } catch (err: any) {
    addToast('error', err?.message || 'Suppression impossible côté backend')
  }
}

const newUser = ref({
  username: '',
  password: '',
  email: '',
  role: 'Agent de Service',
  service: 'Bureau d\'Ordre'
})

const newAssignedRole = ref('Agent de Service')
const roleJustification = ref('')

watch([activeModal, modalUser, selectedDossier], () => {
  if (activeModal.value === 'attribuerRole' && modalUser.value?.role) {
    newAssignedRole.value = modalUser.value.role
  }
  if (activeModal.value === 'modifierDossier' && selectedDossier.value) {
    editDossierForm.value = {
      objet: selectedDossier.value.objet || '',
      expediteur: selectedDossier.value.expediteur || '',
      service: selectedDossier.value.service || '',
      priorite: selectedDossier.value.priorite || 'Normal',
      remarques: ''
    }
  }
})

const addToast = (type: string, msg: string) => store.addToast(type, msg)
const onFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  uploadFiles.value = target.files ? Array.from(target.files) : []
}
const doUpload = async () => {
  if (!uploadFiles.value.length) {
    addToast('error', 'Sélectionnez au moins un fichier avant upload.')
    return
  }
  try {
    await store.uploadDocumentsForSelected(uploadFiles.value, uploadType.value, uploadComment.value)
    addToast('success', `${uploadFiles.value.length} document(s) uploadé(s) et stocké(s) avec succès !`)
    modalOpen.value = false
    uploadFiles.value = []
    uploadComment.value = ''
  } catch {
    addToast('error', 'Upload impossible. Vérifiez le backend/MinIO.')
  }
}
const submitDecision = async () => {
  if (!modalDossier.value?.id) {
    addToast('error', 'Aucun dossier sélectionné')
    return
  }
  const statut = activeModal.value === 'valider' ? WorkflowStatuts.VALIDE : WorkflowStatuts.REJETE
  try {
    await store.transitionDossier(modalDossier.value.id, statut, decisionComment.value)
    addToast(activeModal.value === 'valider' ? 'success' : 'error', activeModal.value === 'valider' ? 'Dossier validé !' : 'Dossier rejeté.')
    decisionComment.value = ''
    modalOpen.value = false
  } catch {
    addToast('error', 'Action impossible côté backend')
  }
}
const submitComment = async () => {
  if (!modalDossier.value?.id) {
    addToast('error', 'Aucun dossier sélectionné')
    return
  }
  if (!commentText.value.trim()) {
    addToast('error', 'Le commentaire est requis')
    return
  }
  try {
    await store.addCommentToDossier(modalDossier.value.id, commentText.value, commentVisibility.value)
    addToast('success', 'Commentaire ajouté et agent notifié.')
    commentText.value = ''
    commentVisibility.value = 'agent'
    modalOpen.value = false
  } catch {
    addToast('error', 'Commentaire non enregistré côté backend')
  }
}
const archiveDossier = async () => {
  if (!selectedDossier.value?.id) {
    addToast('error', 'Aucun dossier sélectionné')
    return
  }
  try {
    await store.transitionDossier(selectedDossier.value.id, WorkflowStatuts.ARCHIVE, archiveNote.value || 'Archivage effectué')
    addToast('success', 'Dossier archivé et verrouillé')
    archiveNote.value = ''
    modalOpen.value = false
  } catch {
    addToast('error', 'Archivage impossible côté backend')
  }
}
const submitNewUser = async () => {
  if (!newUser.value.username || !newUser.value.password || !newUser.value.email) {
    addToast('error', 'Veuillez remplir tous les champs obligatoires.')
    return
  }
  try {
    await store.createNewUser({
      username: newUser.value.username,
      password: newUser.value.password,
      email: newUser.value.email,
      role: newUser.value.role,
      service: newUser.value.service
    })
    addToast('success', 'Utilisateur créé avec succès !')
    modalOpen.value = false
    newUser.value = { username: '', password: '', email: '', role: 'Agent de Service', service: 'Bureau d\'Ordre' }
  } catch (error: any) {
    addToast('error', error?.response?.data?.message || 'Erreur lors de la création.')
  }
}

const submitUpdateDossier = async () => {
  if (!selectedDossier.value?.id) return
  try {
    const payload = {
      titre: editDossierForm.value.objet,
      description: editDossierForm.value.remarques,
      destinataireExterne: editDossierForm.value.expediteur,
      serviceCible: editDossierForm.value.service,
      priorite: editDossierForm.value.priorite.toUpperCase().replace(' ', '_')
    }
    await store.updateDossier(selectedDossier.value.id, payload)
    modalOpen.value = false
  } catch (err) {
    console.error('Update failed:', err)
  }
}

const submitRoleChange = async () => {
  if (!modalUser.value?.id) {
    addToast('error', 'Utilisateur non sélectionné')
    return
  }
  try {
    await store.assignUserRole(modalUser.value.id, newAssignedRole.value)
    addToast('success', 'Rôle mis à jour avec succès !')
    roleJustification.value = ''
    modalOpen.value = false
    modalUser.value = null
  } catch (err: any) {
    addToast('error', err?.response?.data?.message || 'Changement de rôle impossible côté backend')
  }
}
</script>

<template>
  <div class="overlay" v-if="modalOpen" @click.self="modalOpen=false">
    <div class="modal">
      <!-- CREER DOSSIER -->
      <template v-if="activeModal==='creerDossier'">
        <div class="modal-head">
          <div class="modal-title">Créer un nouveau dossier</div>
          <button class="modal-close" @click="modalOpen=false">✕</button>
        </div>
        <div class="modal-body">
          <div class="form-grid form-2" style="gap:14px">
            <div class="form-group"><label class="form-label">Type</label>
              <select class="form-select"><option>Entrant</option><option>Sortant</option><option>Interne</option></select></div>
            <div class="form-group"><label class="form-label">Date réception</label><input type="date" class="form-input" :value="todayISO"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Expéditeur</label><input type="text" class="form-input" placeholder="Nom / Organisme"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet</label><input type="text" class="form-input" placeholder="Objet du dossier..."></div>
            <div class="form-group"><label class="form-label">Service destinataire</label>
              <select class="form-select"><option>Direction Générale</option><option>Service Financier</option><option>Service Technique</option><option>RH</option></select></div>
            <div class="form-group"><label class="form-label">Priorité</label>
              <select class="form-select"><option>Normal</option><option>Urgent</option><option>Très urgent</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Remarques</label><textarea class="form-textarea" placeholder="..."></textarea></div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-primary" @click="modalOpen=false;addToast('success','Dossier créé ! Workflow démarré automatiquement.')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
            Créer & démarrer
          </button>
        </div>
      </template>

      <!-- UPLOAD DOC -->
      <template v-if="activeModal==='uploadDoc'">
        <div class="modal-head"><div class="modal-title">Uploader un document</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div style="border:2px dashed var(--border);border-radius:12px;padding:36px;text-align:center;margin-bottom:16px;background:var(--bg)">
            <svg viewBox="0 0 24 24" fill="none" stroke="var(--blue)" stroke-width="1.5" style="width:40px;height:40px;margin:0 auto 10px;display:block"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
            <div style="font-size:13px;font-weight:600">Déposez vos fichiers ici</div>
            <div style="font-size:11px;color:var(--muted);margin-top:4px">PDF, JPG, PNG — Max 10 Mo par fichier</div>
            <input type="file" class="form-input" style="margin-top:14px" multiple @change="onFileChange">
          </div>
          <div v-if="uploadFiles.length" style="margin:-6px 0 14px 0;font-size:11px;color:var(--muted)">
            <strong style="color:var(--ink)">{{uploadFiles.length}}</strong> fichier(s) sélectionné(s):
            <span style="display:block;margin-top:6px;color:var(--muted)">
              {{ uploadFiles.map(f => f.name).slice(0, 4).join(', ') }}<span v-if="uploadFiles.length > 4">…</span>
            </span>
          </div>
          <div class="form-group"><label class="form-label">Type de document</label>
            <select class="form-select" v-model="uploadType"><option>Document original</option><option>Copie numérisée</option><option>Annexe</option><option>Justificatif</option></select></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Commentaire</label><input type="text" class="form-input" placeholder="Description du document..." v-model="uploadComment"></div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-primary" @click="doUpload" :disabled="!uploadFiles.length">Uploader<span v-if="uploadFiles.length"> ({{uploadFiles.length}})</span></button>
        </div>
      </template>

      <!-- VALIDER -->
      <template v-if="activeModal==='valider'||activeModal==='rejeter'">
        <div class="modal-head">
          <div class="modal-title">{{activeModal==='valider'?'Valider le dossier':'Rejeter le dossier'}}</div>
          <button class="modal-close" @click="modalOpen=false">✕</button>
        </div>
        <div class="modal-body">
          <div style="padding:12px 16px;border-radius:9px;margin-bottom:16px;font-size:12px" :style="{background:activeModal==='valider'?'var(--green-s)':'var(--red-s)',color:activeModal==='valider'?'var(--green)':'var(--red)'}">
            <strong>{{activeModal==='valider'?'✓ Validation':'✗ Rejet'}}</strong> — Cette décision sera tracée dans l'historique du dossier.
          </div>
          <div class="form-group" v-if="modalDossier"><label class="form-label">Dossier concerné</label>
            <input class="form-input" :value="modalDossier.numero+' — '+modalDossier.objet" disabled style="background:var(--bg)"></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Commentaire / Motif</label>
            <textarea class="form-textarea" :placeholder="activeModal==='valider'?'Approuvé sans réserve...':'Motif du rejet...'" v-model="decisionComment"></textarea></div>
          <div class="form-group" style="margin-top:12px" v-if="activeModal==='valider'">
            <label class="form-label">Transmettre ensuite à</label>
            <select class="form-select"><option>Service Financier (Paiement)</option><option>Archivage direct</option><option>Direction Générale</option></select>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn" :class="activeModal==='valider'?'btn-success':'btn-danger'" @click="submitDecision">
            {{activeModal==='valider'?'Confirmer la validation':'Confirmer le rejet'}}
          </button>
        </div>
      </template>

      <!-- COMMENTER -->
      <template v-if="activeModal==='commenter'">
        <div class="modal-head"><div class="modal-title">Ajouter un commentaire</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div class="form-group"><label class="form-label">Commentaire</label><textarea class="form-textarea" style="min-height:120px" placeholder="Votre commentaire pour l'agent..." v-model="commentText"></textarea></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Visibilité</label>
            <select class="form-select" v-model="commentVisibility"><option value="agent">Visible par l'agent</option><option value="interne">Interne — Responsable uniquement</option></select></div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-primary" @click="submitComment">Envoyer</button>
        </div>
      </template>

      <!-- REPONSE -->
      <template v-if="activeModal==='reponse'">
        <div class="modal-head"><div class="modal-title">Rédiger une réponse officielle</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group"><label class="form-label">Destinataire</label><input class="form-input" placeholder="Nom / Service"></div>
            <div class="form-group"><label class="form-label">Type de réponse</label>
              <select class="form-select"><option>Réponse officielle</option><option>Accusé de réception</option><option>Demande de complément</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Objet</label><input class="form-input" placeholder="En réponse à..."></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Corps du message</label><textarea class="form-textarea" style="min-height:110px" placeholder="Rédigez votre réponse..."></textarea></div>
            <div class="form-group"><label class="form-label">Transmission via</label>
              <select class="form-select"><option>Bureau d'Ordre</option><option>Email direct</option><option>Courrier postal</option></select></div>
            <div class="form-group"><label class="form-label">Joindre document</label><input type="file" class="form-input" style="padding:6px"></div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-primary" @click="modalOpen=false;addToast('success','Réponse transmise via le Bureau d\'Ordre.')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
            Envoyer
          </button>
        </div>
      </template>

      <!-- PAIEMENT -->
      <template v-if="activeModal==='paiement'">
        <div class="modal-head"><div class="modal-title">Enregistrer un paiement</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group"><label class="form-label">Réf. dossier</label><input class="form-input" placeholder="CO-2026-XXXX"></div>
            <div class="form-group"><label class="form-label">Date paiement</label><input type="date" class="form-input" :value="todayISO"></div>
            <div class="form-group"><label class="form-label">Montant (DT)</label><input type="number" class="form-input" placeholder="0.00"></div>
            <div class="form-group"><label class="form-label">Mode de paiement</label>
              <select class="form-select"><option>Virement bancaire</option><option>Chèque</option><option>Caisse</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Référence bancaire</label><input class="form-input" placeholder="N° ordre de virement / chèque..."></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Remarques</label><textarea class="form-textarea" placeholder="..."></textarea></div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-success" @click="modalOpen=false;addToast('success','Paiement enregistré ! Statut dossier → Payé')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
            Confirmer le paiement
          </button>
        </div>
      </template>

    <!-- ARCHIVER -->
    <template v-if="activeModal==='archiver'">
        <div class="modal-head"><div class="modal-title">Archiver le dossier</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div style="background:var(--amber-s);border:1px solid #fcd34d;border-radius:9px;padding:14px;font-size:12px;margin-bottom:16px">
            ⚠ Le dossier sera <strong>verrouillé</strong> après archivage. Aucune modification ne sera possible. Conservation selon règles légales.
          </div>
          <div class="form-group"><label class="form-label">Emplacement d'archivage</label>
            <select class="form-select"><option>Archive Cloud — MinIO</option><option>Archive locale</option></select></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Durée de conservation</label>
            <select class="form-select"><option>5 ans</option><option>10 ans</option><option>30 ans</option><option>Permanent</option></select></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Note d'archivage</label><textarea class="form-textarea" placeholder="Commentaire optionnel..." v-model="archiveNote"></textarea></div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-soft-amber" @click="archiveDossier">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/></svg>
            Archiver
          </button>
        </div>
    </template>

    <!-- ARCHIVE DETAILS (CONSULTATION) -->
    <template v-if="activeModal==='archiveDetails'">
      <div class="modal-head">
        <div class="modal-title">Documents du dossier</div>
        <button class="modal-close" @click="modalOpen=false">✕</button>
      </div>
      <div class="modal-body">
        <div v-if="archiveDetailsLoading" style="padding:10px 0;color:var(--muted);font-size:12px">
          Chargement...
        </div>
        <template v-else>
          <div v-if="archiveDetailsDossier" style="background:var(--bg);border-radius:10px;padding:14px;margin-bottom:14px">
            <div style="font-size:10px;color:var(--muted);font-weight:700;letter-spacing:.6px;text-transform:uppercase">Référence</div>
            <div style="font-size:14px;font-weight:800;color:var(--blue)">{{archiveDetailsDossier.numero || ('Dossier #' + archiveDetailsDossier.id)}}</div>
            <div style="margin-top:8px;font-size:12px;font-weight:700">{{archiveDetailsDossier.titre || archiveDetailsDossier.sujet || archiveDetailsDossier.description || 'Sans objet'}}</div>
            <div style="margin-top:4px;font-size:11px;color:var(--muted)">
              Expéditeur: {{archiveDetailsDossier.destinataireExterne || archiveDetailsDossier.expediteur || '-'}}
              · Statut: {{String(archiveDetailsDossier.statut || '').toUpperCase()}}
            </div>
          </div>

          <DocumentsSection
            title="Documents"
            :documents="archiveDetailsDocuments"
            :loading="archiveDetailsLoading"
            :can-delete="canDeleteDocs"
            :chrome="false"
            @open="openDoc"
            @delete="deleteDoc"
          />
        </template>
      </div>
      <div class="modal-foot">
        <button class="btn btn-outline" @click="modalOpen=false">Fermer</button>
      </div>
    </template>

      <!-- NEW USER -->
      <template v-if="activeModal==='newUser'||activeModal==='editUser'">
        <div class="modal-head"><div class="modal-title">{{activeModal==='newUser'?'Créer un utilisateur':'Modifier utilisateur'}}</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Nom d'utilisateur</label>
              <input class="form-input" placeholder="Identifiant" v-model="newUser.username"></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Email</label>
              <input type="email" class="form-input" placeholder="email@bureauordre.com" v-model="newUser.email"></div>
            <div class="form-group"><label class="form-label">Rôle</label>
              <select class="form-select" v-model="newUser.role">
                <option>Administrateur</option><option>Agent Bureau d'Ordre</option><option>Agent de Service</option><option>Responsable</option><option>Agent Financier</option>
              </select></div>
            <div class="form-group"><label class="form-label">Service</label>
              <select class="form-select" v-model="newUser.service">
                <option>Bureau d'Ordre</option><option>Service Technique</option><option>Service Financier</option><option>Direction Générale</option><option>RH</option>
              </select></div>
            <div class="form-group" style="grid-column:1/-1" v-if="activeModal==='newUser'"><label class="form-label">Mot de passe</label>
              <input type="password" class="form-input" placeholder="••••••••" v-model="newUser.password"></div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false;modalUser=null">Annuler</button>
          <button class="btn btn-primary" @click="submitNewUser">
            {{activeModal==='newUser'?'Créer le compte':'Enregistrer'}}
          </button>
        </div>
      </template>

      <!-- ATTRIBUER ROLE -->
      <template v-if="activeModal==='attribuerRole'">
        <div class="modal-head"><div class="modal-title">Attribuer un rôle</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div v-if="modalUser" style="display:flex;align-items:center;gap:10px;padding:12px 14px;background:var(--bg);border-radius:9px;margin-bottom:16px">
            <div class="user-av-sm" :style="{background:modalUser.color,width:'36px',height:'36px',borderRadius:'9px',fontSize:'13px'}">{{modalUser.init}}</div>
            <div><div style="font-weight:700;font-size:13px">{{modalUser.name}}</div><div style="font-size:11px;color:var(--muted)">{{modalUser.email}}</div></div>
          </div>
          <div class="form-group"><label class="form-label">Rôle actuel</label><input class="form-input" :value="modalUser?modalUser.role:''" disabled style="background:var(--bg)"></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Nouveau rôle</label>
            <select class="form-select" v-model="newAssignedRole"><option>Administrateur</option><option>Agent Bureau d'Ordre</option><option>Agent de Service</option><option>Responsable</option><option>Agent Financier</option></select></div>
          <div class="form-group" style="margin-top:12px"><label class="form-label">Justification du changement</label><textarea class="form-textarea" placeholder="Motif du changement de rôle..." v-model="roleJustification"></textarea></div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false;modalUser=null">Annuler</button>
          <button class="btn btn-primary" @click="submitRoleChange">Appliquer le changement</button>
        </div>
      </template>

      <!-- MODIFIER DOSSIER -->
      <template v-if="activeModal==='modifierDossier'">
        <div class="modal-head"><div class="modal-title">Modifier le dossier</div><button class="modal-close" @click="modalOpen=false">✕</button></div>
        <div class="modal-body">
          <div class="form-grid form-2" style="gap:12px">
            <div class="form-group"><label class="form-label">Objet</label><input class="form-input" v-model="editDossierForm.objet"></div>
            <div class="form-group"><label class="form-label">Expéditeur</label><input class="form-input" v-model="editDossierForm.expediteur"></div>
            <div class="form-group"><label class="form-label">Service</label>
              <select class="form-select" v-model="editDossierForm.service"><option>Direction Générale</option><option>Service Financier</option><option>Service Technique</option><option>RH</option></select></div>
            <div class="form-group"><label class="form-label">Priorité</label>
              <select class="form-select" v-model="editDossierForm.priorite"><option>Normal</option><option>Urgent</option><option>Très urgent</option></select></div>
            <div class="form-group" style="grid-column:1/-1"><label class="form-label">Remarques</label><textarea class="form-textarea" placeholder="..." v-model="editDossierForm.remarques"></textarea></div>
          </div>
        </div>
        <div class="modal-foot">
          <button class="btn btn-outline" @click="modalOpen=false">Annuler</button>
          <button class="btn btn-primary" @click="submitUpdateDossier">Enregistrer</button>
        </div>
      </template>

    </div>
  </div>
</template>
