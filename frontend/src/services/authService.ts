import { api, setAuthToken } from './api'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  username: string
}

export const authService = {
  async login(payload: LoginPayload, remember: boolean = true) {
    // Le user-service expose POST /api/users/login (gateway: /api/users/**)
    const { data } = await api.post<LoginResponse>('/api/users/login', payload)
    if (data.token) setAuthToken(data.token, remember)
    return data
  },
  logout() {
    setAuthToken('')
  },
}
