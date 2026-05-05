import { defineStore } from 'pinia'
import { authService } from '../services/authService'
import { dossierService } from '../services/dossierService'
import { userService } from '../services/userService'
import { paiementService } from '../services/paiementService'
import { documentService } from '../services/documentService'
import { workflowService } from '../services/workflowService'
import { setAuthToken } from '../services/api'
import axios from 'axios'

export const useAppStore = defineStore('app', {
  state: () => {
    const now = new Date()
    const storedToken = localStorage.getItem('bo_token') || sessionStorage.getItem('bo_token') || ''
    const storedRemember = !!localStorage.getItem('bo_token')
    return {
      currentRole: 'abo',
      sessionUser: { username: '', email: '', roles: [] as string[] } as any,
      isAuthenticated: !!storedToken,
      authToken: storedToken,
      rememberToken: storedRemember,
      unauthListenerBound: false,
      apiError: '' as string,
      isLoadingDossiers: false,
      activeView: 'abo-dashboard',
      selectedDossier: null as any,
      searchQuery: '',
      userSearch: '',
      selectedPriority: 'Normal',
      modalOpen: false,
      activeModal: '',
      modalDossier: null as any,
      modalUser: null as any,
      toasts: [] as any[],
      toastCounter: 0,
      today: now.toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }),
      todayISO: now.toISOString().split('T')[0],
      wfSteps: [
        { label: 'Réception', done: true, active: false },
        { label: 'Enregistrement', done: true, active: false },
        { label: 'Traitement', done: false, active: true },
        { label: 'Validation', done: false, active: false },
        { label: 'Paiement', done: false, active: false },
        { label: 'Archivage', done: false, active: false },
      ],
      dossiers: [] as any[],
      archives: [] as any[],
      paiements: [] as any[],
      workflowTasks: [] as any[],
      docsCountByDossierId: {} as Record<string, number>,
      archiveDetailsLoading: false,
      archiveDetailsDossier: null as any,
      archiveDetailsDocuments: [] as any[],
      selectedDossierDocuments: [] as any[],
      selectedDossierDocsLoading: false,
      auditLogs: [] as any[],
      adminUsers: [] as any[],
      decisionsResp: [] as any[],
      statsDistrib: [
        { label: 'Archivés', count: 0, pct: 0, color: '#64748b' },
        { label: 'Payés', count: 0, pct: 0, color: '#15803d' },
        { label: 'En validation', count: 0, pct: 0, color: '#be185d' },
        { label: 'En traitement', count: 0, pct: 0, color: '#b45309' },
        { label: 'Rejetés', count: 0, pct: 0, color: '#dc2626' },
      ],
      systemConfig: [
        { key: 'notif', label: 'Notifications email', sub: 'Alertes courriers urgents', on: true },
        { key: '2fa', label: 'Double authentification', sub: 'Obligatoire pour admins', on: true },
        { key: 'audit', label: 'Journal d\'audit', sub: 'Traçabilité complète', on: true },
        { key: 'maint', label: 'Mode maintenance', sub: 'Access restreint utilisateurs', on: false },
        { key: 'archivage', label: 'Archivage automatique', sub: 'Dossiers payés archivés', on: true },
      ],
      services: [
        { name: 'api-gateway', port: ':8080', ok: true }, { name: 'auth-service', port: ':8081', ok: true },
        { name: 'dossier-service', port: ':8082', ok: true }, { name: 'workflow-service', port: ':8083', ok: true },
        { name: 'paiement-service', port: ':8084', ok: true }, { name: 'notification-service', port: ':8085', ok: true },
        { name: 'document-service', port: ':8086', ok: true },
      ],
      activities: [] as any[],
      roles: [
        { key: 'abo', label: 'Agent Bureau d\'Ordre' },
        { key: 'as', label: 'Agent de Service' },
        { key: 'resp', label: 'Responsable' },
        { key: 'af', label: 'Agent Financier' },
        { key: 'admin', label: 'Administrateur' },
      ],
      usersMap: {
        abo: { name: 'Jamal', init: 'AB', color: '#1d4ed8', roleLabel: 'Agent Bureau d\'Ordre' },
        as: { name: 'Karim', init: 'KS', color: '#6366f1', roleLabel: 'Agent de Service' },
        resp: { name: 'Aziz', init: 'MH', color: '#0369a1', roleLabel: 'Responsable  Chef de Service' },
        af: { name: 'Bilal ', init: 'BL', color: '#15803d', roleLabel: 'Agent Financier' },
        admin: { name: 'Fatima', init: 'FM', color: '#dc2626', roleLabel: 'Administrateur Système' },
      } as Record<string, any>,
      navMap: {
        abo: [
          {
            title: 'Tableau de bord', items: [
              { key: 'abo-dashboard', label: 'Tableau de bord', icon: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>' },
            ]
          },
          {
            title: 'Dossiers', items: [
              { key: 'abo-creer', label: 'Créer dossier', icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><line x1="12" y1="18" x2="12" y2="12"/><line x1="9" y1="15" x2="15" y2="15"/>' },
              { key: 'abo-enregistrer', label: 'Enregistrer courrier', icon: '<path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>', badge: '' },
              { key: 'abo-uploader', label: 'Uploader document', icon: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/>' },
              { key: 'abo-consulter', label: 'Consulter dossiers', icon: '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>' },
              { key: 'abo-archive', label: 'Consulter archives', icon: '<polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/>' },
            ]
          },
        ],
        as: [
          {
            title: 'Mes dossiers', items: [
              { key: 'as-dashboard', label: 'Tableau de bord', icon: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>' },
              { key: 'as-traiter', label: 'Traiter dossier', icon: '<path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>', badge: '', badgeClass: 'amber' },
              { key: 'as-repondre', label: 'Rédiger réponse', icon: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>' },
            ]
          },
        ],
        resp: [
          {
            title: 'Validation', items: [
              { key: 'resp-dashboard', label: 'File de validation', icon: '<path d="M9 11l3 3L22 4"/>', badge: '', badgeClass: 'amber' },
              { key: 'resp-rapport', label: 'Générer rapport', icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>' },
            ]
          },
        ],
        af: [
          {
            title: 'Paiements', items: [
              { key: 'af-dashboard', label: 'Paiements', icon: '<line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>', badge: '', badgeClass: 'amber' },
              { key: 'af-enregistrer', label: 'Enregistrer paiement', icon: '<rect x="1" y="4" width="22" height="16" rx="2"/>' },
              { key: 'af-justificatif', label: 'Générer justificatif', icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>' },
            ]
          },
        ],
        admin: [
          {
            title: 'Administration', items: [
              { key: 'admin-dashboard', label: 'Tableau de bord', icon: '<rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>' },
              { key: 'admin-users', label: 'Utilisateurs', icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>', badge: '', badgeClass: 'green' },
              { key: 'admin-stats', label: 'Statistiques', icon: '<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>' },
            ]
          },
        ],
      } as Record<string, any>,
      defaultViews: { abo: 'abo-dashboard', as: 'as-dashboard', resp: 'resp-dashboard', af: 'af-dashboard', admin: 'admin-dashboard' } as Record<string, string>,
    }
  },
  getters: {
    currentUser: (state) => {
      const base = state.usersMap[state.currentRole] || { name: 'Utilisateur', init: 'U', color: '#64748b', roleLabel: 'Utilisateur' }
      const username = state.sessionUser?.username || base.name
      const init = String(username || 'U').trim().slice(0, 2).toUpperCase() || base.init
      return { ...base, name: username, init }
    },
    currentNav: (state) => state.navMap[state.currentRole] || [],
    filteredDossiers: (state) => {
      if (!state.searchQuery) return state.dossiers;
      const q = state.searchQuery.toLowerCase();
      return state.dossiers.filter((d: any) => d.objet.toLowerCase().includes(q) || d.numero.toLowerCase().includes(q) || d.expediteur.toLowerCase().includes(q));
    },
    filteredAdminUsers: (state) => {
      if (!state.userSearch) return state.adminUsers;
      const q = state.userSearch.toLowerCase();
      return state.adminUsers.filter((u: any) => u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q));
    },
    // Statistiques calculÃ©es dynamiquement
    statsCounters: (state) => {
      const totalDossiers = state.dossiers.length
      const actifsUsers = state.adminUsers.filter((u: any) => u.active).length
      
      // Dossiers ce mois (calculÃ© sur dateReception)
      const now = new Date()
      const currentMonth = now.getMonth()
      const currentYear = now.getFullYear()
      const dossiersCeMois = state.dossiers.filter((d: any) => {
        if (!d.dateReception) return false
        const parts = d.dateReception.split('/')
        if (parts.length === 3) {
          const dDate = new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]))
          return dDate.getMonth() === currentMonth && dDate.getFullYear() === currentYear
        }
        return false
      }).length

      // Taux de traitement (Dossiers archivés / Total)
      const traitees = state.dossiers.filter((d: any) => d.statutKey === 'archive').length
      const tauxTraitement = totalDossiers > 0 ? Math.round((traitees / totalDossiers) * 100) : 0

      return {
        totalDossiers,
        actifsUsers,
        dossiersCeMois,
        tauxTraitement,
        stockage: (totalDossiers * 0.5).toFixed(1) + 'M', // Simulation simple: 0.5MB par dossier
        uptime: 'En ligne'
      }
    },
    aboStats: (state) => {
      return {
        crees: state.dossiers.length,
        enTraitement: state.dossiers.filter((d: any) => d.statutKey === 'traitement').length,
        archives: state.archives.length,
        urgents: state.dossiers.filter((d: any) => d.urgent).length
      }
    },
    asStats: (state) => {
      const assigned = state.dossiers.filter((d: any) => d.statutKey === 'enregistrement' || d.statutKey === 'traitement' || d.statutKey === 'rejete')
      return {
        recus: assigned.length,
        enCours: assigned.filter((d: any) => d.statutKey === 'traitement' || d.statutKey === 'rejete').length,
        transmis: state.dossiers.filter((d: any) => d.statutKey === 'validation').length,
        urgents: assigned.filter((d: any) => d.urgent).length
      }
    },
    respStats: (state) => {
      const valides = state.dossiers.filter((d: any) => d.statutKey === 'paiement' || d.statutKey === 'archive').length
      const total = state.dossiers.length || 1
      return {
        enAttente: state.dossiers.filter((d: any) => d.statutKey === 'validation').length,
        validesMois: valides,
        taux: Math.round((valides / total) * 100),
        rejetes: state.dossiers.filter((d: any) => d.statutKey === 'rejete').length,
        avecCommentaire: state.dossiers.filter((d: any) => d.historique?.length > 0).length
      }
    },
    afStats: (state) => {
      return {
        enAttente: state.paiements.filter((p: any) => !p.paid).length,
        payesMois: state.paiements.filter((p: any) => p.paid).length,
        dossiersPayes: state.dossiers.filter((d: any) => d.statutKey === 'paiement').length
      }
    },
    candidateGroupForRole: (state) => {
      const r = String(state.currentRole || '')
      if (r === 'abo') return 'BO'
      if (r === 'as') return 'SERVICE'
      if (r === 'resp') return 'RESPONSABLE'
      if (r === 'af') return 'FINANCIER'
      return ''
    },
  },
  actions: {
    resolveRoleFromRoles(rawRoles: any) {
      const rolesArr = Array.isArray(rawRoles)
        ? rawRoles
        : rawRoles && typeof rawRoles[Symbol.iterator] === 'function'
          ? Array.from(rawRoles as any)
          : []
      const roles = rolesArr.map((r: any) => String(r || '').toUpperCase())
      const has = (...keys: string[]) => roles.some(r => keys.some(k => r.includes(k)))

      // Priorité: admin > responsable > financier > service > BO
      if (has('ADMIN', 'ADMIN_SYSTEME', 'ROLE_ADMIN')) return 'admin'
      if (has('RESP', 'RESPONSABLE', 'RESPONSABLE_HIERARCHIQUE', 'ROLE_RESP')) return 'resp'
      if (has('FIN', 'FINANCIER', 'AGENT_FINANCIER', 'ROLE_FIN')) return 'af'
      if (has('SERVICE', 'AGENT_SERVICE', 'ROLE_SERVICE')) return 'as'
      if (has('BUREAU', 'ORDRE', 'AGENT_BUREAU', 'AGENT_BO', 'ROLE_BO')) return 'abo'
      return 'abo'
    },
    applySessionUser(user: any) {
      this.sessionUser = {
        username: user?.username || this.sessionUser.username,
        email: user?.email || this.sessionUser.email,
        roles: Array.isArray(user?.roles) ? user.roles : Array.from(user?.roles || []),
      }
      const resolved = this.resolveRoleFromRoles(this.sessionUser.roles)
      this.currentRole = resolved
      this.activeView = this.defaultViews[resolved] || 'abo-dashboard'
      this.selectedDossier = null as any
    },
    async initializeApp() {
      if (!this.unauthListenerBound && typeof window !== 'undefined') {
        window.addEventListener('bo:unauthorized', () => {
          // Evite l'état "connecté" avec un token invalide (401 en boucle).
          this.authToken = ''
          this.isAuthenticated = false
          this.selectedDossier = null as any
          this.addToast('error', 'Session expirée ou accès non autorisé. Merci de vous reconnecter.')
        })
        this.unauthListenerBound = true
      }
      if (this.authToken) {
        setAuthToken(this.authToken, this.rememberToken)
        this.isAuthenticated = true
      }
      if (this.isAuthenticated) {
        // Charge l'utilisateur connectÃ© pour dÃ©terminer son rÃ´le et verrouiller l'UI sur ce rÃ´le.
        try {
          const me = await userService.me()
          this.applySessionUser(me)
        } catch {
          // Token invalide ou backend down => l'interceptor gÃ¨re le logout.
        }
        await Promise.all([
          this.fetchDossiers(),
          this.fetchArchives(),
          this.fetchPaiements(),
          this.fetchMyWorkflowTasks()
        ])
        if (this.currentRole === 'admin') {
          await this.fetchUsers()
        }
      }
    },
    async fetchMyWorkflowTasks() {
      try {
        const group = (this as any).candidateGroupForRole || ''
        if (!group) {
          this.workflowTasks = []
          return
        }
        this.workflowTasks = await workflowService.listTasks({ candidateGroup: group })
      } catch (e) {
        console.warn('fetchMyWorkflowTasks failed:', e)
        this.workflowTasks = []
      }
    },
    async fetchWorkflowTasksByDossier(dossierId: number | string) {
      const group = (this as any).candidateGroupForRole || ''
      const list = await workflowService.listTasks({
        dossierId: Number(dossierId),
        candidateGroup: group || undefined,
      })
      return Array.isArray(list) ? list : []
    },
    async startWorkflowForDossier(dossierId: number | string) {
      await workflowService.start(dossierId)
      await Promise.all([this.fetchDossiers(), this.fetchMyWorkflowTasks()])
    },
    async completeWorkflowTaskForDossier(dossierId: number | string, taskDefinitionKey: string, variables?: Record<string, any>) {
      const tasks = await this.fetchWorkflowTasksByDossier(dossierId)
      const task = tasks.find((t: any) => String(t?.taskDefinitionKey || '') === String(taskDefinitionKey || ''))
      if (!task?.id) {
        throw new Error(`Aucune tÃ¢che workflow '${taskDefinitionKey}' trouvÃ©e pour ce dossier.`)
      }
      await workflowService.completeTask(String(task.id), variables || {})
      await Promise.all([this.fetchDossiers(), this.fetchArchives(), this.fetchPaiements(), this.fetchMyWorkflowTasks()])
    },
    async completeEnregistrementWorkflow(dossierId: number | string) {
      await this.completeWorkflowTaskForDossier(dossierId, 'UserTask_Enregistrement')
    },
    async completeTraitementWorkflow(dossierId: number | string, commentaire?: string) {
      const vars: any = {}
      if (commentaire) vars.commentaire = commentaire
      await this.completeWorkflowTaskForDossier(dossierId, 'UserTask_Traitement', vars)
      if (commentaire) {
        try {
          await dossierService.comment(dossierId, { commentaire, visibilite: 'interne' })
        } catch (e) {
          console.warn('comment trace failed:', e)
        }
      }
    },
    async completeValidationWorkflow(dossierId: number | string, isValidated: boolean, commentaire?: string) {
      const vars: any = { isValidated: !!isValidated }
      if (commentaire) vars.commentaire = commentaire
      await this.completeWorkflowTaskForDossier(dossierId, 'UserTask_Validation', vars)
      if (commentaire) {
        try {
          await dossierService.comment(dossierId, { commentaire, visibilite: 'interne' })
        } catch (e) {
          console.warn('comment trace failed:', e)
        }
      }
    },
    async completePaiementWorkflow(dossierId: number | string, montant: number, commentaire?: string) {
      const vars: any = { montant: Number(montant) }
      if (!isFinite(vars.montant) || vars.montant <= 0) {
        throw new Error('Montant invalide (doit Ãªtre > 0).')
      }
      if (commentaire) vars.commentaire = commentaire
      await this.completeWorkflowTaskForDossier(dossierId, 'UserTask_Paiement', vars)
      if (commentaire) {
        try {
          await dossierService.comment(dossierId, { commentaire, visibilite: 'interne' })
        } catch (e) {
          console.warn('comment trace failed:', e)
        }
      }
    },
    async completeArchivageWorkflow(dossierId: number | string, commentaire?: string) {
      const vars: any = {}
      if (commentaire) vars.commentaire = commentaire
      await this.completeWorkflowTaskForDossier(dossierId, 'UserTask_Archivage', vars)
      if (commentaire) {
        try {
          await dossierService.comment(dossierId, { commentaire, visibilite: 'interne' })
        } catch (e) {
          console.warn('comment trace failed:', e)
        }
      }
    },
    async fetchUsers() {
      try {
        const apiUsers = await userService.list()
        if (Array.isArray(apiUsers)) {
          const roleToUi = (roles: any): { label: string; color: string } => {
            const arr = Array.isArray(roles) ? roles : (roles && typeof roles === 'object' ? Array.from(roles as any) : [])
            const roleName = (arr[0] || '').toString().toUpperCase()
            if (roleName.includes('ADMIN')) return { label: 'Administrateur', color: '#dc2626' }
            if (roleName.includes('RESP')) return { label: 'Responsable', color: '#0369a1' }
            if (roleName.includes('FINAN')) return { label: 'Agent Financier', color: '#15803d' }
            if (roleName.includes('SERVICE')) return { label: 'Agent de Service', color: '#6366f1' }
            if (roleName.includes('BUREAU') || roleName.includes('ORDRE') || roleName.includes('BO')) return { label: "Agent Bureau d'Ordre", color: '#1d4ed8' }
            return { label: roleName || 'Utilisateur', color: '#64748b' }
          }
          this.adminUsers = apiUsers.map(u => ({
            id: u.id,
            name: u.username,
            email: u.email,
            init: u.username.substring(0, 2).toUpperCase(),
            color: '#' + Math.floor(Math.random() * 16777215).toString(16),
            role: roleToUi((u as any).roles).label,
            service: (u as any).service || 'Non assigné',
            active: (u as any).active !== false,
            lastSeen: 'Récemment',
            roleColor: roleToUi((u as any).roles).color
          }))
        }
      } catch (error) {
        console.error('Erreur fetchUsers:', error)
      }
    },
    formatDateDisplay(v: any) {
      if (!v) return '-'
      if (typeof v === 'string' && v.includes('-')) {
        const dt = new Date(v)
        return Number.isNaN(dt.getTime()) ? v : dt.toLocaleDateString('fr-FR')
      }
      return String(v)
    },
    async fetchArchives() {
      try {
        const apiArchives = await dossierService.listByStatut('ARCHIVE')
        if (Array.isArray(apiArchives)) {
          this.archives = apiArchives.map(d => ({
            id: d.id,
            ref: d.numero,
            objet: d.titre || d.sujet || d.description || 'Sans objet',
            from: d.destinataireExterne || d.expediteur || '-',
            date: d.archivedAt ? this.formatDateDisplay(d.archivedAt) : this.formatDateDisplay(d.dateReception),
            operateur: 'Système',
            docs: this.docsCountByDossierId[String(d.id)] ?? 0
          }))
          // Remplit les compteurs de documents en arriÃ¨re-plan (N appels, limitÃ©).
          void this.prefetchArchiveDocCounts(30)
        }
      } catch (error) {
        console.error('Erreur fetchArchives:', error)
      }
    },
    async prefetchArchiveDocCounts(max: number = 30) {
      const targets = (Array.isArray(this.archives) ? this.archives : []).slice(0, Math.max(0, max))
      for (const a of targets) {
        const id = a?.id
        if (id == null) continue
        if (this.docsCountByDossierId[String(id)] != null && this.docsCountByDossierId[String(id)] > 0) {
          continue
        }
        try {
          await this.fetchDocumentsByDossier(id)
        } catch {
          // ignore (service docs indisponible)
        }
      }
    },
    async fetchDocumentsByDossier(dossierId: number | string) {
      const docs = await documentService.listByDossier(dossierId)
      const count = Array.isArray(docs) ? docs.length : 0
      this.docsCountByDossierId[String(dossierId)] = count

      const idx = this.archives.findIndex((a: any) => String(a.id) === String(dossierId))
      if (idx !== -1) {
        this.archives[idx].docs = count
      }
      return docs
    },
    async loadSelectedDossierDocuments(dossierId: number | string) {
      this.selectedDossierDocsLoading = true
      try {
        const docs = await this.fetchDocumentsByDossier(dossierId)
        this.selectedDossierDocuments = Array.isArray(docs) ? docs : []
      } finally {
        this.selectedDossierDocsLoading = false
      }
    },
    async loadArchiveDetails(dossierId: number | string) {
      this.archiveDetailsLoading = true
      try {
        const [dossier, docs] = await Promise.all([
          dossierService.getById(dossierId),
          this.fetchDocumentsByDossier(dossierId),
        ])
        this.archiveDetailsDossier = dossier
        this.archiveDetailsDocuments = Array.isArray(docs) ? docs : []
      } finally {
        this.archiveDetailsLoading = false
      }
    },
    async openArchiveDetails(dossierId: number | string) {
      this.openModal('archiveDetails', { id: dossierId })
      await this.loadArchiveDetails(dossierId)
    },
    async openDocument(doc: any) {
      const id = doc?.id
      if (id == null) throw new Error('Document invalide')
      try {
        const blob = await documentService.download(id)
        const url = URL.createObjectURL(blob)

        // Try open in new tab. If blocked, fallback to forced download.
        const w = window.open(url, '_blank', 'noopener,noreferrer')
        if (!w) {
          const a = document.createElement('a')
          a.href = url
          a.download = doc?.nomFichier || `document-${id}`
          document.body.appendChild(a)
          a.click()
          a.remove()
        }

        setTimeout(() => URL.revokeObjectURL(url), 60_000)
      } catch (e: any) {
        const status = e?.response?.status
        if (status === 401) {
          throw new Error('Non autorisé (401). Reconnectez-vous puis réessayez.')
        }

        // When axios responseType=blob, error body is also a Blob.
        const data = e?.response?.data
        if (data && typeof Blob !== 'undefined' && data instanceof Blob) {
          let extracted: string | null = null
          try {
            const txt = await data.text()
            if (txt && txt.trim()) {
              try {
                const parsed = JSON.parse(txt)
                if (parsed?.message && typeof parsed.message === 'string') {
                  extracted = parsed.message.trim()
                }
              } catch {
                extracted = txt.trim()
              }
            }
          } catch {
            // ignore blob read errors
          }
          if (extracted) {
            throw new Error(`${extracted}${status ? ` (HTTP ${status})` : ''}`)
          }
        }

        const fallback = status ? `Impossible d'ouvrir le document (HTTP ${status})` : `Impossible d'ouvrir le document`
        throw new Error(fallback)
      }
    },
    async deleteDocument(doc: any) {
      const id = doc?.id
      const dossierId = doc?.dossierId ?? this.archiveDetailsDossier?.id ?? this.selectedDossier?.id
      if (id == null) throw new Error('Document invalide')

      await documentService.delete(id)

      // Refresh current modal/list if open.
      if (dossierId != null) {
        try {
          const docs = await this.fetchDocumentsByDossier(dossierId)
          if (this.archiveDetailsDossier && String(this.archiveDetailsDossier.id) === String(dossierId)) {
            this.archiveDetailsDocuments = Array.isArray(docs) ? docs : []
          }
          if (this.selectedDossier && String(this.selectedDossier.id) === String(dossierId)) {
            this.selectedDossierDocuments = Array.isArray(docs) ? docs : []
          }
        } catch {
          // ignore
        }
      }

      // Also remove from local arrays (best-effort) to keep UI responsive.
      this.archiveDetailsDocuments = (Array.isArray(this.archiveDetailsDocuments) ? this.archiveDetailsDocuments : [])
        .filter((d: any) => String(d?.id) !== String(id))
    },
    async fetchPaiements() {
      try {
        const apiPaiements = await paiementService.list()
        if (Array.isArray(apiPaiements)) {
          const dossierById = new Map<string, any>(
            (Array.isArray(this.dossiers) ? this.dossiers : []).map((d: any) => [String(d.id), d])
          )
          this.paiements = apiPaiements.map((p: any) => {
            const dossier = dossierById.get(String(p.dossierId))
            const paiementStatut = String(p.statut || '').toUpperCase()
            const dossierStatutRaw = String(dossier?.statutRaw || dossier?.statut || '').toUpperCase()
            const paid = dossierStatutRaw === 'PAYE' || paiementStatut === 'VALIDE'
            const echeance = p.datePaiement ? String(p.datePaiement).slice(0, 10) : '-'
            return {
              id: p.id,
              dossierId: p.dossierId,
              ref: dossier?.numero || `Dossier #${p.dossierId}`,
              objet: dossier?.objet || 'Paiement dossier',
              from: dossier?.expediteur || '-',
              montant: `${p.montant} DT`,
              echeance,
              statut: paiementStatut,
              paid,
            }
          })
        }
      } catch (error) {
        console.error('Erreur fetchPaiements:', error)
        // Fallback UI: si le microservice paiement n'est pas accessible,
        // on affiche au moins les dossiers "VALIDE" (Ã  payer) cÃ´tÃ© agent financier.
        const dossiers = Array.isArray(this.dossiers) ? this.dossiers : []
        this.paiements = dossiers
          .filter((d: any) => ['VALIDE', 'PAYE'].includes(String(d.statutRaw || '').toUpperCase()))
          .map((d: any) => ({
            id: `local-${d.id}`,
            dossierId: d.id,
            ref: d.numero,
            objet: d.objet,
            from: d.expediteur,
            montant: '-',
            echeance: '-',
            statut: String(d.statutRaw || '').toUpperCase() === 'PAYE' ? 'VALIDE' : 'EN_ATTENTE',
            paid: String(d.statutRaw || '').toUpperCase() === 'PAYE',
          }))
      }
    },
    async fetchDossiers() {
      this.isLoadingDossiers = true
      this.apiError = ''
      try {
        const apiDossiers = await dossierService.list()
        if (Array.isArray(apiDossiers)) {
          const mapPriorite = (raw: any) => {
            const p = String(raw || '').toUpperCase()
            if (p.includes('TRES_URGENT')) return { label: 'Très urgent', level: 3, urgent: true }
            if (p.includes('URGENT')) return { label: 'Urgent', level: 3, urgent: true }
            if (p.includes('HAUTE')) return { label: 'Urgent', level: 2, urgent: true }
            return { label: 'Normal', level: 1, urgent: false }
          }

          this.dossiers = apiDossiers.map((d: any, index: number) => {
            const statutRaw = d.statut ?? d.statutDossier ?? 'OUVERT'
            const prioriteMapped = mapPriorite(d.priorite)
            return {
              id: d.id ?? index + 1,
              numero: d.numero ?? `CO-API-${index + 1}`,
              objet: d.titre || d.sujet || d.description || 'Sans objet',
              expediteur: d.destinataireExterne || (d.userId ? `User ${d.userId}` : 'Non renseigné'),
              dateReception: this.formatDateDisplay(d.dateReception),
              service: d.serviceCible || 'Non affecté',
              statutRaw: String(statutRaw).toUpperCase(),
              statut: this.getStatutLabel(String(statutRaw)),
              statutKey: this.mapStatutKey(String(statutRaw)),
              priorite: prioriteMapped.label,
              prioriteLevel: prioriteMapped.level,
              deadline: this.formatDateDisplay(d.deadlineAt),
              urgent: prioriteMapped.urgent,
              agent: 'Agent',
              agentInit: 'AG',
              agentColor: '#6366f1',
              historique: [],
            }
          })

          // Mise à jour des stats de distribution en fonction des données réelles
          const countStatut = (key: string) => this.dossiers.filter((d: any) => d.statutKey === key).length
          const total = this.dossiers.length || 1
          this.statsDistrib = [
            { label: 'Archivés', count: countStatut('archive'), pct: Math.round((countStatut('archive') / total) * 100), color: '#64748b' },
            { label: 'Payés', count: countStatut('paiement'), pct: Math.round((countStatut('paiement') / total) * 100), color: '#15803d' },
            { label: 'En validation', count: countStatut('validation'), pct: Math.round((countStatut('validation') / total) * 100), color: '#be185d' },
            { label: 'En traitement', count: countStatut('traitement'), pct: Math.round((countStatut('traitement') / total) * 100), color: '#b45309' },
            { label: 'Rejetés', count: countStatut('rejete'), pct: Math.round((countStatut('rejete') / total) * 100), color: '#dc2626' },
          ]
        }
      } catch (error: any) {
        this.apiError = error?.message || 'Impossible de charger les dossiers depuis le backend.'
      } finally {
        this.isLoadingDossiers = false
      }
    },
    mapStatutKey(statut?: string) {
      const value = (statut || '').toUpperCase()
      if (value.includes('OUVERT')) return 'enregistrement'
      if (value.includes('RECU') || value.includes('RECEPTION')) return 'reception'
      if (value.includes('ENREG')) return 'enregistrement'
      if (value.includes('EN_COURS')) return 'validation'
      if (value.includes('TRAITEMENT')) return 'traitement'
      // Dossier "VALIDE" => en attente de paiement (et non plus en validation).
      if (value === 'VALIDE') return 'paiement'
      if (value.includes('VALID')) return 'validation'
      if (value.includes('PAY')) return 'paiement'
      if (value.includes('ARCH')) return 'archive'
      if (value.includes('CLOTURE')) return 'archive'
      if (value.includes('REJET')) return 'rejete'
      return 'reception'
    },
    async login(username: string, password: string, remember: boolean = true) {
      this.rememberToken = remember
      const data = await authService.login({ username, password }, remember)
      this.authToken = data.token
      this.isAuthenticated = true
      // RÃ©cupÃ¨re le rÃ´le rÃ©el depuis le backend (pas de sÃ©lection manuelle cÃ´tÃ© UI).
      try {
        const me = await userService.me()
        this.applySessionUser(me)
      } catch {
        // fallback si /me indisponible: reste sur abo
        this.sessionUser = { username, email: '', roles: [] }
        this.currentRole = 'abo'
        this.activeView = this.defaultViews.abo
      }
      await Promise.all([
        this.fetchDossiers(),
        this.fetchArchives(),
        this.fetchPaiements(),
        this.fetchMyWorkflowTasks()
      ])
      if (this.currentRole === 'admin') {
        await this.fetchUsers()
      }
      return data
    },
    logout() {
      authService.logout()
      this.authToken = ''
      this.isAuthenticated = false
      this.rememberToken = true
      this.selectedDossier = null as any
      this.addToast('info', 'Déconnection avec succès')
    },
    getStatutLabel(statut: string) {
      const upper = String(statut || '').toUpperCase()
      if (upper === 'VALIDE') return 'Validé (à payer)'
      if (upper === 'PAYE') return 'Payé'
      if (upper === 'REJETE') return 'Rejeté'
      const key = this.mapStatutKey(statut)
      const labels: Record<string, string> = {
        reception: 'Reception',
        enregistrement: 'Enregistrement',
        traitement: 'Traitement',
        validation: 'Validation',
        paiement: 'Paiement',
        archive: 'Archivage',
        rejete: 'Rejeté',
      }
      return labels[key] || statut
    },
    applyLocalTransition(dossierId: number | string, statut: string, commentaire?: string) {
      const idx = this.dossiers.findIndex((d: any) => String(d.id) === String(dossierId))
      if (idx === -1) return
      const d = this.dossiers[idx]
      d.statutRaw = String(statut || '').toUpperCase()
      d.statutKey = this.mapStatutKey(statut)
      d.statut = this.getStatutLabel(statut)
      const historyItem = {
        id: Date.now(),
        action: `Transition vers ${d.statut}`,
        user: this.currentUser?.name || 'Utilisateur',
        date: new Date().toLocaleString('fr-FR'),
        bg: '#dbeafe',
        color: '#1d4ed8',
        icon: '<polyline points="20 6 9 17 4 12"/>',
        commentaire: commentaire || '',
      }
      d.historique = Array.isArray(d.historique) ? [historyItem, ...d.historique] : [historyItem]
      if (this.selectedDossier && String(this.selectedDossier.id) === String(dossierId)) {
        this.selectedDossier = d
      }
    },
    async transitionDossier(dossierId: number | string, statut: string, commentaire?: string) {
      // Mode Camunda: on complète les user tasks au lieu de changer directement le statut.
      const upper = String(statut || '').toUpperCase()
      if (upper === 'VALIDE') {
        await this.completeValidationWorkflow(dossierId, true, commentaire)
        return
      }
      if (upper === 'REJETE') {
        await this.completeValidationWorkflow(dossierId, false, commentaire)
        return
      }
      if (upper === 'PAYE') {
        throw new Error('En mode Camunda, utilisez completePaiementWorkflow(dossierId, montant, commentaire).')
      }
      if (upper === 'ARCHIVE') {
        await this.completeArchivageWorkflow(dossierId, commentaire)
        return
      }
      if (upper === 'EN_COURS') {
        // Ancien libellé UI utilisé par l'agent service: correspond à la complétion de la tâche "Traitement".
        await this.completeTraitementWorkflow(dossierId, commentaire)
        return
      }
      if (upper === 'ENREGISTRE') {
        await this.completeEnregistrementWorkflow(dossierId)
        return
      }
      throw new Error(`Transition '${upper}' non supportée via Camunda.`)
    },
    async transitionSelectedDossier(statut: string, commentaire?: string) {
      if (!this.selectedDossier?.id) throw new Error('Aucun dossier selectionné')
      await this.transitionDossier(this.selectedDossier.id, statut, commentaire)
    },
    async addCommentToDossier(dossierId: number | string, commentaire: string, visibilite?: string) {
      await dossierService.comment(dossierId, { commentaire, visibilite })
      const idx = this.dossiers.findIndex((d: any) => String(d.id) === String(dossierId))
      if (idx === -1) return
      const d = this.dossiers[idx]
      const historyItem = {
        id: Date.now(),
        action: 'Commentaire ajouté',
        user: this.currentUser?.name || 'Utilisateur',
        date: new Date().toLocaleString('fr-FR'),
        bg: '#ede9fe',
        color: '#6d28d9',
        icon: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>',
        commentaire,
        visibilite: visibilite || 'agent',
      }
      d.historique = Array.isArray(d.historique) ? [historyItem, ...d.historique] : [historyItem]
      if (this.selectedDossier && String(this.selectedDossier.id) === String(dossierId)) {
        this.selectedDossier = d
      }
    },
    async createDossier(payload: any) {
      const data = await dossierService.create(payload)
      // Démarre le workflow Camunda: Réception(auto) -> tâche BO "Enregistrement"
      if (data?.id != null) {
        try {
          await workflowService.start(data.id)
        } catch (e) {
          console.warn('workflow start failed:', e)
        }
      }
      await Promise.all([this.fetchDossiers(), this.fetchMyWorkflowTasks()])
      return data
    },
    async uploadDossierDocument(dossierId: number | string, file: File, type: string = 'original', commentaire?: string) {
      const data = await dossierService.uploadDocument(dossierId, file, { type, commentaire })

      // Trace locale (historique) pour l'UI, mÃªme si le backend ne stocke pas le commentaire upload.
      const idx = this.dossiers.findIndex((d: any) => String(d.id) === String(dossierId))
      if (idx !== -1) {
        const d = this.dossiers[idx]
        const historyItem = {
          id: Date.now(),
          action: 'Document uploadÃ©',
          user: this.currentUser?.name || 'Utilisateur',
          date: new Date().toLocaleString('fr-FR'),
          bg: '#dbeafe',
          color: '#1d4ed8',
          icon: '<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>',
          commentaire: commentaire || '',
        }
        d.historique = Array.isArray(d.historique) ? [historyItem, ...d.historique] : [historyItem]
        if (this.selectedDossier && String(this.selectedDossier.id) === String(dossierId)) {
          this.selectedDossier = d
        }
      }

      return data
    },
    async uploadDossierDocuments(dossierId: number | string, files: File[], type: string = 'original', commentaire?: string) {
      const list = Array.isArray(files) ? files : []
      if (!list.length) throw new Error('Aucun fichier sélectionné')

      // Backend endpoint supports 1 file per request, so we upload sequentially.
      const results: any[] = []
      for (const f of list) {
        // Keep per-file comment if user typed one (optional).
        // The UI history will show one entry per file.
        // eslint-disable-next-line no-await-in-loop
        const r = await this.uploadDossierDocument(dossierId, f, type, commentaire)
        results.push(r)
      }

      // Refresh document count for archive/list views (best-effort).
      try {
        await this.fetchDocumentsByDossier(dossierId)
      } catch {
        // ignore
      }
      return results
    },
    async uploadDocumentForSelected(file: File, type: string = 'original', commentaire?: string) {
      if (!this.selectedDossier?.id) throw new Error('Aucun dossier sélectionné')
      return this.uploadDossierDocument(this.selectedDossier.id, file, type, commentaire)
    },
    async uploadDocumentsForSelected(files: File[], type: string = 'original', commentaire?: string) {
      if (!this.selectedDossier?.id) throw new Error('Aucun dossier sélectionné')
      return this.uploadDossierDocuments(this.selectedDossier.id, files, type, commentaire)
    },
    async createNewUser(payload: any) {
      const roleLabel = String(payload?.role || '').trim()
      const roleName = (() => {
        const v = roleLabel.toLowerCase()
        if (v.includes('admin')) return 'ROLE_ADMIN'
        if (v.includes('responsable')) return 'ROLE_RESPONSABLE'
        if (v.includes('financier')) return 'ROLE_AGENT_FINANCIER'
        if (v.includes('service')) return 'ROLE_AGENT_SERVICE'
        if (v.includes('bureau')) return 'ROLE_AGENT_BUREAU_ORDRE'
        return 'ROLE_AGENT_BUREAU_ORDRE'
      })()

      await userService.create({
        username: payload?.username,
        password: payload?.password,
        email: payload?.email,
        service: payload?.service,
        roleName,
      })
      await this.fetchUsers()
    },
    async assignUserRole(userId: number | string, roleLabel: string) {
      const v = String(roleLabel || '').toLowerCase()
      const roleName =
        v.includes('admin') ? 'ROLE_ADMIN'
          : v.includes('responsable') ? 'ROLE_RESPONSABLE'
            : v.includes('financier') ? 'ROLE_AGENT_FINANCIER'
              : v.includes('service') ? 'ROLE_AGENT_SERVICE'
                : 'ROLE_AGENT_BUREAU_ORDRE'
      await userService.setRole(userId, roleName)
      await this.fetchUsers()
    },
    switchRole(role: string) {
      // DÃ©sactivÃ©: le rÃ´le vient du backend et n'est pas sÃ©lectionnable cÃ´tÃ© UI.
      this.addToast('error', 'Changement de rôle interdit. Déconnectez-vous pour changer de compte.')
    },
    selectDossier(d: any) { this.selectedDossier = d },
    openModal(name: string, data: any = null) {
      this.activeModal = name;
      this.modalOpen = true;
      if (data && (data.numero || data.id)) this.modalDossier = data;
      if (data && data.email) this.modalUser = data;
    },
    async updateDossier(dossierId: number | string, payload: any) {
      try {
        await dossierService.update(dossierId, payload)
        await this.fetchDossiers()
        // Après avoir rafraîchi la liste, assurez-vous que selectedDossier pointe vers la version à jour
        const foundUpdatedDossier = this.dossiers.find((d: any) => String(d.id) === String(dossierId))
        if (foundUpdatedDossier) {
          this.selectedDossier = foundUpdatedDossier
        } else {
          // Si le dossier mis à jour n'est plus trouvé (ex: filtré, supprimé), désélectionnez-le
          this.selectedDossier = null
        }

        this.addToast('success', 'Dossier mis à jour avec succès !')
      } catch (error) {
        this.addToast('error', 'Erreur lors de la mise à jour du dossier.')
        throw error
      }
    },
    async sendEmailResponse(payload: { to: string, subject: string, body: string }) {
      try {
        await axios.post('/api/notifications/email', payload)
        console.log('Envoi de l\'e-mail via notification-service:', payload)
        this.addToast('success', 'E-mail envoyé avec succès !')
      } catch (error) {
        this.addToast('error', 'Échec de l\'envoi de l\'e-mail.')
        throw error
      }
    },
    addToast(type: string, msg: string) {
      const id = ++this.toastCounter;
      this.toasts.push({ id, type, msg });
      setTimeout(() => { this.toasts = this.toasts.filter(t => t.id !== id) }, 3500);
    },
  }
})
