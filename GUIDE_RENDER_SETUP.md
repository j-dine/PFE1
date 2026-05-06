# 🚀 Guide complet - Créer une BD PostgreSQL sur Render

## ✅ Étape 1 : Créer un compte Render (si vous n'en avez pas)

1. Allez sur **https://render.com**
2. Cliquez sur **"Sign Up"**
3. Choisissez la méthode de connexion :
   - GitHub (recommandé)
   - Google
   - Email
4. Suivez les instructions de création de compte

---

## ✅ Étape 2 : Créer la base de données PostgreSQL

### 2.1 - Dans le Dashboard Render

1. Une fois connecté, cliquez sur **"New +"** (en haut à gauche)
2. Sélectionnez **"PostgreSQL"**

### 2.2 - Configurer la base de données

| Champ | Valeur | Notes |
|-------|--------|-------|
| **Name** | `workflowdb` | Nom de la base |
| **PostgreSQL Version** | 15 (par défaut) | Acceptez la version par défaut |
| **Region** | Sélectionnez la plus proche | Ex: Germany (eu-central-1) |
| **Datadog API Key** | Laisser vide | Optionnel |

3. Cliquez sur **"Create Database"**

⏳ **Attendre 5-10 minutes** pour que la BD soit prête

---

## ✅ Étape 3 : Récupérer les credentials

### 3.1 - Accéder aux informations de connexion

1. Une fois la BD créée, cliquez dessus
2. Allez à l'onglet **"Info"** (ou en haut de la page)
3. Vous verrez trois sections :
   - **Internal Database URL**
   - **External Database URL**
   - **Connection Details**

### 3.2 - Copier les informations

**IMPORTANT :** Utilisez l'**INTERNAL DATABASE URL** (pas externe)

```
Exemple de structure :
postgresql://postgres:YOUR_PASSWORD@dpg-xxx123.render.com/workflowdb
```

Extrayez et notez :
- **URL complète** : `postgresql://postgres:YOUR_PASSWORD@dpg-xxx123.render.com/workflowdb`
- **Username** : `postgres`
- **Password** : `YOUR_PASSWORD` (extrait de l'URL)
- **Host** : `dpg-xxx123.render.com`
- **Port** : `5432` (par défaut)
- **Database** : `workflowdb`

---

## ✅ Étape 4 : Configurer les secrets dans Render

### 4.1 - Aller aux paramètres du projet

1. Allez dans votre **Dashboard Render**
2. Si vous n'avez pas encore créé de projet, cliquez sur **"New Blueprint"**
3. Connectez GitHub et sélectionnez `j-dine/PFE1`

### 4.2 - Ajouter les secrets

1. Une fois le projet créé, cliquez dessus
2. Allez à l'onglet **"Environment"**
3. Cliquez sur **"Add Secret"** (ou **"New Secret"**)

### 4.3 - Entrer les trois secrets

#### Secret 1 : Database URL
```
Name: database-url
Value: postgresql://postgres:YOUR_PASSWORD@dpg-xxx123.render.com/workflowdb
```

#### Secret 2 : Username
```
Name: database-username
Value: postgres
```

#### Secret 3 : Password
```
Name: database-password
Value: YOUR_PASSWORD
```

---

## ✅ Étape 5 : Vérifier la connexion (optionnel)

### Utiliser pgAdmin pour tester

1. Vous pouvez installer **pgAdmin** localement
2. Connectez-vous avec les credentials Render
3. Testez la connexion à distance

```
Host: dpg-xxx123.render.com
Port: 5432
Username: postgres
Password: YOUR_PASSWORD
Database: workflowdb
```

---

## ✅ Étape 6 : Déployer le Blueprint

1. Dans Render Dashboard → **"New"** → **"Blueprint"**
2. Connectez GitHub et sélectionnez `j-dine/PFE1`
3. Configurez :
   - **Branch** : `main`
   - **Environment variables** : Les secrets sont déjà configurés
4. Cliquez sur **"Deploy"**

---

## 📋 Résumé des informations à noter

Créez un fichier `.env` local (NE PAS COMMITTER) avec :

```env
# Database Render
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@dpg-xxx123.render.com/workflowdb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=YOUR_PASSWORD

# JWT Secret (généré automatiquement par Render)
JWT_SECRET=auto-generated-by-render

# API Gateway
API_GATEWAY_URL=https://api-gateway-[hash].onrender.com

# Frontend
FRONTEND_URL=https://[nom-projet].onrender.com

# Grafana
GRAFANA_URL=https://grafana-[hash].onrender.com
GRAFANA_USER=admin
GRAFANA_PASSWORD=admin
```

---

## ⚠️ Points importants

✅ **À FAIRE :**
- Notez les credentials quelque part de sûr
- Utilisez l'URL **INTERNE** (dpg-xxx.render.com)
- Gardez les secrets dans Render, pas dans le code
- Testez la connexion avant de déployer

❌ **À NE PAS FAIRE :**
- Ne committer pas les credentials dans Git
- Ne pas utiliser l'URL **EXTERNE** pour les services Render
- Ne pas utiliser les credentials locaux (postgres/postgres)
- Ne pas partager les passwords

---

## 🔧 Troubleshooting

### "Connection refused"
→ Vérifier que vous utilisez l'URL INTERNE

### "Authentication failed"
→ Vérifier le password dans l'URL

### "Database does not exist"
→ Vérifier que `workflowdb` est créée

### Les services ne trouvent pas la BD
→ Vérifier que les secrets sont configurés correctement dans Render

---

## 📞 Support

Si vous avez des problèmes :
1. Vérifiez les logs dans Render Dashboard
2. Testez la connexion avec pgAdmin
3. Vérifiez que tous les secrets sont configurés
4. Redéployez le Blueprint
