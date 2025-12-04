# UI & double vérification par courriel

Objectif : prouver l’expérience utilisateur complète avec double étape email et notifications SSE sans alourdir le backend.

## 1. Flux à couvrir
- **Signup (étape 1)** : formulaire `email + password + profil` → appel `POST /auth/signup` → afficher état `PENDING` et `traceId`.
- **Vérification courriel (étape 2)** : écran dédié pour saisir l’OTP reçu par email → `POST /auth/verify-email` → état `ACTIVE`.
- **Connexion step-up** : `POST /auth/login`; si `202` avec `operationId`, déclencher l’écran OTP.
- **Notifications temps réel** : souscrire `EventSource` à `/api/v1/notifications/stream` (JWT requis) pour afficher OTP envoyés, exécutions d’ordres, dépôts validés.

## 2. Ajustements UI (page démo)
- Ajouter une section « Vérification courriel » avec champ OTP + bouton « Valider ».
- Afficher l’état du compte et la dernière `traceId` dans le panneau Journal.
- Unifier le Journal pour consommer à la fois les réponses HTTP et les messages SSE (type + payload + horodatage).
- Thème rapide : police accentuée (ex : `Space Grotesk`), boutons contrastés, cartes claires pour les formulaires.

## 3. Points backend à exposer côté UI
- Endpoints déjà existants `POST /api/v1/auth/signup`, `/auth/verify-email`, `/auth/login`, `/deposits`, `/orders`.
- Flux SSE `GET /api/v1/notifications/stream` (JWT) et `GET /api/v1/market/stream?symbol=AAPL` pour le ruban marché.
- Inclure le header `Trace-Id` dans les réponses (activer `server.tracing.include-response-headers=true` si supporté).

## 4. Preuve à mettre dans le rapport
- Capture signup → mail OTP → écran de validation → statut `ACTIVE`.
- Capture du panneau Journal montrant un `execution.cree` reçu en SSE.
- Capture d’écran du flux SSE marché (ruban qui bouge).
- Courte note expliquant que le double step email repose sur l’événement `otp.demande` consommé par `notification-service`.
