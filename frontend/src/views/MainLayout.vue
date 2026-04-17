<script setup lang="ts">
import { computed, ref } from 'vue'
import { useAppStore } from '../stores/appStore'

const store = useAppStore()

const currentUser = computed(() => store.currentUser)
const currentNav = computed(() => store.currentNav)
const activeView = computed(() => store.activeView)
const isAuthenticated = computed(() => store.isAuthenticated)

const setActiveView = (view: string) => { store.activeView = view }
const logout = () => store.logout()

const loginUsername = ref('')
const loginPassword = ref('')
const loginError = ref('')
const showPassword = ref(false)
const rememberMe = ref(true)
const isLoggingIn = ref(false)

const doLogin = async () => {
  loginError.value = ''
  try {
    isLoggingIn.value = true
    await store.login(loginUsername.value, loginPassword.value, rememberMe.value)
  } catch (e: any) {
    loginError.value = e?.message || 'Connexion impossible'
  } finally {
    isLoggingIn.value = false
  }
}
</script>

<template>
  <div
    v-if="!isAuthenticated"
    class="login-overlay"
    @click.self="loginError=''"
    role="dialog"
    aria-modal="true"
    aria-label="Connexion"
  >
    <div class="login-card">
      <div class="login-brand">
        <div class="login-logo" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M12 2l7 4v6c0 5-3 9-7 10C8 21 5 17 5 12V6l7-4z" />
            <path d="M9.5 12l1.8 1.8L15.8 9" />
          </svg>
        </div>
        <div class="login-title">Connectez-vous à votre compte</div>
        <div class="login-sub">Entrez votre identifiant et votre mot de passe pour vous connecter.</div>
      </div>

      <form class="login-form" @submit.prevent="doLogin">
        <div class="form-group">
          <label class="form-label">Identifiant</label>
          <div class="login-field">
            <input
              class="form-input login-input"
              v-model="loginUsername"
              placeholder="Votre identifiant"
              autocomplete="username"
            />
            <svg class="login-ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Mot de passe</label>
          <div class="login-field">
            <input
              class="form-input login-input"
              :type="showPassword ? 'text' : 'password'"
              v-model="loginPassword"
              placeholder="Votre mot de passe"
              autocomplete="current-password"
            />
            <button
              class="login-eye"
              type="button"
              @click="showPassword = !showPassword"
              :aria-label="showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
                <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-6 0-10-7-10-7a21.8 21.8 0 0 1 5.06-6.94" />
                <path d="M1 1l22 22" />
                <path d="M9.9 9.9a3 3 0 0 0 4.24 4.24" />
                <path d="M14.12 14.12L9.88 9.88" />
                <path d="M6.1 6.1A10.94 10.94 0 0 1 12 5c6 0 10 7 10 7a21.9 21.9 0 0 1-3.2 4.2" />
              </svg>
            </button>
          </div>
        </div>

        <div v-if="loginError" class="login-alert" role="status">
          {{ loginError }}
        </div>

        <div class="login-row">
          <label class="login-check">
            <input type="checkbox" v-model="rememberMe" />
            <span>Se souvenir de moi</span>
          </label>
          <button class="login-link" type="button" @click="store.addToast('info','Réinitialisation mot de passe: à implémenter')">
            Mot de passe oublié ?
          </button>
        </div>

        <button class="btn btn-primary login-submit" type="submit" :disabled="isLoggingIn">
          {{ isLoggingIn ? 'Connexion...' : 'Connexion' }}
        </button>

        <div class="login-legal">
          <button class="login-link" type="button">Politique de confidentialité</button>
          <span class="login-dot">•</span>
          <button class="login-link" type="button">Conditions générales</button>
        </div>
      </form>
    </div>
  </div>

  <div v-else>
    <!-- SESSION BAR (role is locked to the authenticated user) -->
    <div class="role-bar">
      <span class="role-bar-label">Rôle :</span>
      <span class="role-pill active" style="cursor:default">{{ currentUser.roleLabel }}</span>
      <div class="role-bar-right">
        <div class="online-dot"></div>
        {{ currentUser.name }} - En ligne
      </div>
    </div>

    <div class="shell">
      <!-- SIDEBAR -->
      <aside class="sidebar">
        <div class="sb-head">
          <div class="sb-brand">Bureau <span>d'Ordre</span></div>
        </div>
        <div class="sb-user">
          <div class="sb-av" :style="{background: currentUser.color}">{{ currentUser.init }}</div>
          <div>
            <div class="sb-name">{{ currentUser.name }}</div>
            <div class="sb-role">{{ currentUser.roleLabel }}</div>
          </div>
        </div>
        <nav class="sb-nav">
          <div class="nav-section" v-for="section in currentNav" :key="section.title">
            <div class="nav-sec-title">{{ section.title }}</div>
            <div
              v-for="item in section.items"
              :key="item.key"
              class="nav-item"
              :class="{active: activeView === item.key}"
              @click="setActiveView(item.key)"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="item.icon"></svg>
              {{ item.label }}
              <span class="nav-badge" :class="item.badgeClass" v-if="item.badge">{{ item.badge }}</span>
            </div>
          </div>
        </nav>
        <div class="sb-footer">
          <button class="sb-logout" @click="logout">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Déconnexion
          </button>
        </div>
      </aside>

      <!-- MAIN CONTENT -->
      <main class="main">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.login-overlay{
  position:fixed;
  inset:0;
  display:flex;
  align-items:center;
  justify-content:center;
  padding:24px;
  background:
    radial-gradient(900px 500px at 10% 10%, rgba(29,78,216,.18), transparent 55%),
    radial-gradient(900px 500px at 90% 20%, rgba(15,118,110,.12), transparent 60%),
    radial-gradient(800px 500px at 70% 95%, rgba(180,83,9,.14), transparent 55%),
    linear-gradient(135deg, #f8fafc 0%, #f3f4f6 45%, #f1f0ed 100%);
  overflow:auto;
}
.login-card{
  width:min(460px, 96vw);
  background:rgba(255,255,255,.92);
  border:1px solid var(--border);
  border-radius:18px;
  box-shadow:0 24px 90px rgba(0,0,0,.16);
  backdrop-filter:blur(10px);
  overflow:hidden;
}
.login-brand{
  padding:22px 24px 12px;
  text-align:center;
}
.login-logo{
  width:44px;
  height:44px;
  margin:2px auto 12px;
  border-radius:14px;
  display:flex;
  align-items:center;
  justify-content:center;
  color:#0f172a;
  background:
    radial-gradient(18px 18px at 30% 25%, rgba(255,255,255,.85), rgba(255,255,255,0) 60%),
    linear-gradient(135deg, rgba(29,78,216,.22), rgba(29,78,216,.06));
  border:1px solid rgba(29,78,216,.22);
}
.login-logo svg{width:22px;height:22px}
.login-title{
  font-size:18px;
  font-weight:800;
  letter-spacing:-.2px;
}
.login-sub{
  margin-top:6px;
  font-size:12px;
  color:var(--muted);
}
.login-form{
  padding:14px 24px 20px;
}
.login-field{position:relative}
.login-input{padding-right:48px}
.login-ic{
  position:absolute;
  right:12px;
  top:50%;
  transform:translateY(-50%);
  width:18px;
  height:18px;
  color:var(--faint);
  pointer-events:none;
}
.login-eye{
  position:absolute;
  right:8px;
  top:50%;
  transform:translateY(-50%);
  width:32px;
  height:32px;
  border:none;
  background:transparent;
  color:var(--muted);
  border-radius:8px;
  cursor:pointer;
  display:flex;
  align-items:center;
  justify-content:center;
}
.login-eye:hover{background:var(--bg); color:var(--ink)}
.login-eye svg{width:18px;height:18px}
.login-row{
  display:flex;
  align-items:center;
  justify-content:space-between;
  gap:10px;
  margin-top:12px;
}
.login-check{
  display:flex;
  align-items:center;
  gap:8px;
  font-size:11px;
  color:var(--muted);
  user-select:none;
}
.login-check input{accent-color:var(--blue)}
.login-link{
  border:none;
  background:transparent;
  padding:0;
  color:var(--blue);
  font-size:11px;
  font-weight:700;
  cursor:pointer;
}
.login-link:hover{text-decoration:underline}
.login-submit{
  width:100%;
  justify-content:center;
  margin-top:14px;
  padding:10px 16px;
  border-radius:10px;
}
.login-submit:disabled{opacity:.7; cursor:not-allowed}
.login-alert{
  margin-top:10px;
  padding:10px 12px;
  border-radius:10px;
  background:var(--red-s);
  color:var(--red);
  font-weight:700;
  font-size:11px;
  border:1px solid rgba(220,38,38,.18);
}
.login-legal{
  margin-top:14px;
  display:flex;
  align-items:center;
  justify-content:center;
  gap:10px;
  color:var(--muted);
}
.login-dot{font-size:12px; color:var(--faint)}
</style>

