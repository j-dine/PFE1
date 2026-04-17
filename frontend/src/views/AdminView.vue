<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '../stores/appStore'

const store = useAppStore()

const activeView = computed(() => store.activeView)
const auditLogs = computed(() => store.auditLogs)
const systemConfig = computed(() => store.systemConfig)
const services = computed(() => store.services)
const adminUsers = computed(() => store.adminUsers)
const filteredAdminUsers = computed(() => store.filteredAdminUsers)
const statsDistrib = computed(() => store.statsDistrib)
const statsCounters = computed(() => store.statsCounters)

const userSearch = computed({
  get: () => store.userSearch,
  set: (val) => { store.userSearch = val }
})

const openModal = (name: string, data?: any) => store.openModal(name, data)
const addToast = (type: string, msg: string) => store.addToast(type, msg)
</script>

<template>
  <div class="topbar">
    <div class="topbar-title">
      <span v-if="activeView==='admin-dashboard'">Administration système</span>
      <span v-if="activeView==='admin-users'">Gestion des utilisateurs</span>
      <span v-if="activeView==='admin-stats'">Statistiques globales</span>
    </div>
    <div class="topbar-actions">
      <button class="btn btn-primary btn-sm" @click="openModal('newUser')" v-if="activeView==='admin-users'">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><line x1="16" y1="11" x2="20" y2="11"/><line x1="18" y1="9" x2="18" y2="13"/></svg>
        Nouvel utilisateur
      </button>
    </div>
  </div>
  <div class="content">
    <template v-if="activeView==='admin-dashboard'">
      <div style="background:var(--amber-s);border:1px solid #fcd34d;border-left:4px solid var(--amber);border-radius:10px;padding:13px 18px;display:flex;align-items:center;gap:12px;margin-bottom:18px;font-size:12px">
        <svg viewBox="0 0 24 24" fill="none" stroke="var(--amber)" stroke-width="2" style="width:18px;height:18px;flex-shrink:0"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <div><strong>Alerte sécurité :</strong> 3 tentatives de connexion échouées détectées. IP : 192.168.1.87</div>
      </div>
      <div class="stats stats-4">
        <div class="stat-card sc-blue"><div class="stat-label">Utilisateurs actifs</div><div class="stat-value">{{statsCounters.actifsUsers}}</div><div class="stat-delta">Total inscrits</div></div>
        <div class="stat-card sc-green"><div class="stat-label">Dossiers ce mois</div><div class="stat-value">{{statsCounters.dossiersCeMois}}</div><div class="stat-delta">Mois en cours</div></div>
        <div class="stat-card sc-amber"><div class="stat-label">Stockage estimé</div><div class="stat-value" style="font-size:22px">{{statsCounters.stockage}}</div><div class="stat-delta">Utilisation BD</div></div>
        <div class="stat-card sc-teal"><div class="stat-label">Statut système</div><div class="stat-value" style="font-size:20px">OK</div><div class="stat-delta up">{{statsCounters.uptime}}</div></div>
      </div>
      <div class="col-2">
        <div class="card">
          <div class="card-head"><div class="card-title">Journal d'audit — Activité récente</div></div>
          <div>
            <div v-for="log in auditLogs" :key="log.id" style="display:flex;gap:10px;padding:11px 18px;border-bottom:1px solid var(--bg);font-size:11px;align-items:flex-start">
              <span style="padding:2px 7px;border-radius:4px;font-size:9px;font-weight:800;flex-shrink:0;font-family:monospace" :style="{background:log.level==='ERROR'?'var(--red-s)':log.level==='WARN'?'var(--amber-s)':'var(--blue-s)',color:log.level==='ERROR'?'var(--red)':log.level==='WARN'?'var(--amber)':'var(--blue)'}">{{log.level}}</span>
              <div style="font-family:monospace;font-size:10px;color:var(--muted);flex-shrink:0;padding-top:1px">{{log.time}}</div>
              <div><div style="color:var(--ink)">{{log.msg}}</div><div style="color:var(--faint);margin-top:1px">{{log.user}} — {{log.ip}}</div></div>
            </div>
          </div>
        </div>
        <div>
          <div class="card" style="margin-bottom:14px">
            <div class="card-head"><div class="card-title">Configuration système</div></div>
            <div>
              <div v-for="cfg in systemConfig" :key="cfg.key" style="display:flex;align-items:center;justify-content:space-between;padding:12px 18px;border-bottom:1px solid var(--bg);font-size:12px">
                <div>
                  <div style="font-weight:600">{{cfg.label}}</div>
                  <div style="font-size:10px;color:var(--muted)">{{cfg.sub}}</div>
                </div>
                <div class="toggle" :class="cfg.on?'on':'off'" @click="cfg.on=!cfg.on">
                  <div class="toggle-thumb"></div>
                </div>
              </div>
            </div>
          </div>
          <div class="card">
            <div class="card-head"><div class="card-title">Services microservices</div></div>
            <div>
              <div v-for="svc in services" :key="svc.name" style="display:flex;align-items:center;gap:10px;padding:10px 18px;border-bottom:1px solid var(--bg);font-size:12px">
                <div class="online-dot" :style="{background:svc.ok?'#4ade80':'var(--red)'}"></div>
                <span style="flex:1;font-weight:600">{{svc.name}}</span>
                <span style="font-size:10px;color:var(--muted)">{{svc.port}}</span>
                <span style="font-size:10px;font-weight:700" :style="{color:svc.ok?'var(--green)':'var(--red)'}">{{svc.ok?'Opérationnel':'Hors ligne'}}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
    <template v-if="activeView==='admin-users'">
      <div class="card">
        <div class="card-head">
          <div><div class="card-title">Gestion des utilisateurs</div><div class="card-sub">{{adminUsers.length}} comptes — Rôles et permissions</div></div>
          <div style="display:flex;gap:8px">
            <input type="text" class="form-input" style="width:200px;font-size:11px;padding:6px 10px" placeholder="Rechercher..." v-model="userSearch">
            <select class="form-select" style="width:130px;font-size:11px;padding:6px 10px">
              <option>Tous rôles</option><option>Admin</option><option>Agent BO</option><option>Agent Service</option><option>Responsable</option><option>Agent Financier</option>
            </select>
          </div>
        </div>
        <table class="tbl">
          <thead><tr><th>Utilisateur</th><th>Rôle</th><th>Service</th><th>Statut</th><th>Dernière connexion</th><th>Actions</th></tr></thead>
          <tbody>
            <tr v-for="u in filteredAdminUsers" :key="u.id">
              <td>
                <div style="display:flex;align-items:center;gap:9px">
                  <div class="user-av-sm" :style="{background:u.color}">{{u.init}}</div>
                  <div><div style="font-weight:700;font-size:12px">{{u.name}}</div><div style="font-size:10px;color:var(--muted)">{{u.email}}</div></div>
                </div>
              </td>
              <td>
                <span class="chip" :style="{background:u.roleColor+'22',color:u.roleColor}">{{u.role}}</span>
              </td>
              <td style="font-size:11px;color:var(--muted)">{{u.service}}</td>
              <td>
                <span class="badge" :class="u.active?'b-paiement':'b-rejete'">
                  {{u.active?'Actif':'Suspendu'}}
                </span>
              </td>
              <td style="font-size:10px;color:var(--muted)">{{u.lastSeen}}</td>
              <td>
                <div style="display:flex;gap:5px">
                  <button class="btn btn-outline btn-sm" @click="openModal('editUser',u)">Modifier</button>
                  <button class="btn btn-soft-amber btn-sm" @click="openModal('attribuerRole',u)">Rôle</button>
                  <button class="btn btn-soft-danger btn-sm" @click="u.active=!u.active;addToast(u.active?'success':'info',u.active?'Compte activé':'Compte suspendu')">
                    {{u.active?'Suspendre':'Activer'}}
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
    <template v-if="activeView==='admin-stats'">
      <div class="stats stats-3">
        <div class="stat-card sc-blue"><div class="stat-label">Total dossiers</div><div class="stat-value">{{statsCounters.totalDossiers}}</div><div class="stat-delta">Depuis lancement</div></div>
        <div class="stat-card sc-green"><div class="stat-label">Taux de traitement</div><div class="stat-value">{{statsCounters.tauxTraitement}}%</div><div class="stat-delta">↑ Basé sur archives</div></div>
        <div class="stat-card sc-amber"><div class="stat-label">Délai moyen</div><div class="stat-value" style="font-size:22px">Auto</div><div class="stat-delta">Réception → Archivage</div></div>
      </div>
      <div class="card">
        <div class="card-head"><div class="card-title">Répartition par statut — Mars 2026</div></div>
        <div style="padding:20px">
          <div v-for="s in statsDistrib" :key="s.label" style="margin-bottom:14px">
            <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:6px">
              <span style="font-weight:600">{{s.label}}</span>
              <span style="color:var(--muted)">{{s.count}} dossiers ({{s.pct}}%)</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" :style="{width:s.pct+'%',background:s.color}"></div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
