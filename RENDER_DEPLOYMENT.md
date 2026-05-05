# 🚀 Déploiement sur Render

## Configuration requise

### 1. Créer la base de données PostgreSQL
- Aller sur [render.com](https://render.com)
- **New** → **PostgreSQL**
- Nom: `workflowdb`
- Plan: **Starter** (gratuit)
- Créer la base de données

### 2. Récupérer les informations de connexion
Dans votre base de données PostgreSQL → onglet **Info** :
- **Internal Database URL** : `postgresql://postgres:xxxx@dpg-xxx.render.com/workflowdb`
- **Username** : `postgres`
- **Password** : `xxxx` (généré automatiquement)

### 3. Configurer les secrets
Dans votre projet Render → onglet **Environment** → **Add Secret** :

| Secret Name | Value |
|-------------|--------|
| `database-url` | `postgresql://postgres:xxxx@dpg-xxx.render.com/workflowdb` |
| `database-username` | `postgres` |
| `database-password` | `xxxx` |

### 4. Déployer le Blueprint
- **New** → **Blueprint**
- Sélectionner le repository `j-dine/PFE1`
- Render lira automatiquement `render.yaml`

## Services déployés
- ✅ **discovery-service** - Service Eureka
- ✅ **api-gateway** - Passerelle API
- ✅ **user-service** - Gestion utilisateurs
- ✅ **dossier-service** - Gestion dossiers
- ✅ **document-service** - Gestion documents
- ✅ **workflow-service** - Moteur Camunda
- ✅ **paiement-service** - Gestion paiements
- ✅ **notification-service** - Notifications
- ✅ **frontend** - Interface Vue.js
- ✅ **prometheus** - Métriques
- ✅ **grafana** - Dashboard monitoring

## URLs importantes
- **Frontend** : `https://[nom-projet].onrender.com`
- **API Gateway** : `https://api-gateway-[hash].onrender.com`
- **Grafana** : `https://grafana-[hash].onrender.com` (admin/admin)</content>
<parameter name="filePath">c:\Users\jamal\Desktop\PFE1\RENDER_DEPLOYMENT.md