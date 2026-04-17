import { api } from './api'

export interface UserApi {
  id: number | string
  username: string
  email: string
  role?: string
  service?: string
  active?: boolean
  roles?: string[] | Set<string> | any
}

export const userService = {
  async list() {
    const { data } = await api.get<UserApi[]>('/api/admin/users')
    return data
  },
  async me() {
    const { data } = await api.get<UserApi>('/api/users/me')
    return data
  },
  async create(payload: Record<string, unknown>) {
    const { data } = await api.post('/api/admin/users', payload)
    return data
  },
  async setRole(id: number | string, roleName: string) {
    const { data } = await api.put(`/api/admin/users/${id}/role`, { roleName })
    return data
  },
  async delete(id: number | string) {
    await api.delete(`/api/admin/users/${id}`)
  }
}
