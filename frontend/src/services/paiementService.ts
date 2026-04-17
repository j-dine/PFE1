import { api } from './api'

export interface PaiementApi {
  id: number | string
  dossierId: number | string
  montant: number
  statut: string
  datePaiement?: string
  referenceBancaire?: string
}

export const paiementService = {
  async list() {
    const { data } = await api.get<PaiementApi[]>('/api/paiements')
    return data
  },
  async create(payload: Record<string, unknown>) {
    const { data } = await api.post('/api/paiements', payload)
    return data
  },
  async updateStatut(id: number | string, statut: string) {
    const { data } = await api.patch(`/api/paiements/${id}/statut`, null, {
      params: { statut }
    })
    return data
  }
}
