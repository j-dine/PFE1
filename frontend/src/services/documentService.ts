import { api } from './api'

export interface DocumentApi {
  id: number | string
  nomFichier: string
  type?: string
  urlStorage?: string
  dateUpload?: string
  dossierId: number | string
}

export const documentService = {
  async listByDossier(dossierId: number | string) {
    const { data } = await api.get<DocumentApi[]>(`/api/documents/dossier/${dossierId}`)
    return data
  },
  async getById(id: number | string) {
    const { data } = await api.get<DocumentApi>(`/api/documents/${id}`)
    return data
  },
  async download(id: number | string) {
    const { data } = await api.get(`/api/documents/${id}/download`, {
      responseType: 'blob',
    })
    return data as Blob
  },
  async delete(id: number | string) {
    await api.delete(`/api/documents/${id}`)
  },
}
