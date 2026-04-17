import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL,
  timeout: 15000,
})

let authToken = localStorage.getItem('bo_token') || sessionStorage.getItem('bo_token') || ''

export const setAuthToken = (token: string, remember: boolean = true) => {
  authToken = token
  if (token) {
    if (remember) {
      localStorage.setItem('bo_token', token)
      sessionStorage.removeItem('bo_token')
    } else {
      sessionStorage.setItem('bo_token', token)
      localStorage.removeItem('bo_token')
    }
  } else {
    localStorage.removeItem('bo_token')
    sessionStorage.removeItem('bo_token')
  }
}

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 401 = token absent/expiré/invalide (souvent après redémarrage ou expiration).
    // On purge le token et on notifie l'app pour forcer une reconnexion.
    if (error?.response?.status === 401) {
      setAuthToken('')
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('bo:unauthorized'))
      }
    }
    return Promise.reject(error)
  }
)
