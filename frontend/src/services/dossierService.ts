import { api } from './api'

export interface DossierApi {
  id: number | string
  numero: string
  // Champs historiquement utilises par l'UI (anciennes versions)
  objet?: string
  expediteur?: string
  service?: string
  // Champs réels renvoyés par dossier-service (DossierDTO)
  dateReception?: string
  sujet?: string
  titre?: string
  description?: string
  statut: string
  priorite?: string
  typeDossier?: string
  serviceCible?: string
  destinataireExterne?: string
  canalReception?: string
  typeCourrier?: string
  deadlineAt?: string
  dateExpedition?: string
  modeExpedition?: string
  retentionYears?: number
  archivedAt?: string
  userId?: number | string
  dateCreation?: string
  locked?: boolean
}

export const dossierService = {
  async list() {
    const { data } = await api.get<DossierApi[]>('/api/dossiers')
    return data
  },
  async getById(id: number | string) {
    const { data } = await api.get<DossierApi>(`/api/dossiers/${id}`)
    return data
  },
  async listByStatut(statut: string) {
    const { data } = await api.get<DossierApi[]>(`/api/dossiers/statut/${statut}`)
    return data
  },
  async create(payload: Record<string, unknown>) {
    const { data } = await api.post('/api/dossiers', payload)
    return data
  },
  async update(id: number | string, payload: Record<string, unknown>) {
    const { data } = await api.put(`/api/dossiers/${id}`, payload)
    return data
  },
  async transition(dossierId: number | string, payload: { statut: string; commentaire?: string }) {
    // Mise à jour statut "simple" (controller: PUT /api/dossiers/{id}/statut?statut=...)
    // Archivage (controller: POST /api/dossiers/{id}/archive?retentionYears=...&commentaire=...)
    if (payload.statut === 'ARCHIVE') {
      const { data } = await api.post(`/api/dossiers/${dossierId}/archive`, null, {
        params: { commentaire: payload.commentaire },
      })
      return data
    }

    const { data } = await api.put(`/api/dossiers/${dossierId}/statut`, null, {
      params: { statut: payload.statut },
    })
    return data
  },
  async comment(dossierId: number | string, payload: { commentaire: string; visibilite?: string }) {
    const { data } = await api.post(`/api/dossiers/${dossierId}/comments`, null, {
      params: {
        commentaire: payload.commentaire,
        visibilite: payload.visibilite,
      },
    })
    return data
  },
  async uploadDocument(dossierId: number | string, file: File, metadata?: { type?: string; commentaire?: string }) {
    const formData = new FormData()
    formData.append('file', file)
    // On passe dossierId et type en query params car le controller utilise @RequestParam
    const { data } = await api.post(`/api/documents/upload`, formData, {
      params: {
        dossierId,
        type: metadata?.type || 'original',
      },
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return data
  },
}
