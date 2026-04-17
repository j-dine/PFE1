import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import { useAppStore } from './stores/appStore'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const store = useAppStore(pinia)
store.initializeApp()

app.mount('#app')
