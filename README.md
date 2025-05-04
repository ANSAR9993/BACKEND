## 1. Objectif du Projet

Concevoir une application web métier pour :

- **Suivi & Alerte** : Certificats SSL/TLS (acheté et généré) avec alertes email.
- **Gestion Equipment** : Inventaire des devices (firewalls) par site, ports, versions, garantie.
- **Gestion Marchés** : Suivi des marchés liés aux devices.

## 2. Rôles et Responsabilités

- **Product Owner** : Valide le cahier des charges et priorise les fonctionnalités.
- **Architecte Technique** : Définit l’architecture globale, choix technos et schémas.
- **Développeur Back-end** : Implémente les API Spring Boot (certificats, devices, marchés, alertes).
- **Développeur Front-end** : Crée l’application React/Vite (UI, appels API, gestion d’état).
- **DevOps/Test** : CI/CD, tests unitaires/integration, déploiement, monitoring.

## 3. Périmètre et Fonctions Clés

| Fonction                     | Description                                    | Priorité |
| ---------------------------- | ---------------------------------------------- | -------- |
| Import certificats générés   | Charger depuis DB interne pour suivi & alerte  | Haute    |
| CRUD certificats achetés     | Enregistrer, modifier, lister certifs externes | Haute    |
| Alerte email 30j avant exp.  | Scheduler + SMTP                               | Haute    |
| CRUD Organisations & Devices | Org, device, ports, garantie                   | Moyenne  |
| Gestion Marchés Terminés     | Filtrer, lister, associer devices              | Moyenne  |
| Audit Log                    | Traçabilité des actions critiques              | Basse    |

## 4. Architecture Technique

1. **Backend** (Spring Boot 3.4.4, Java 21)

    - Spring Data JPA, SQL Server
    - Spring Security JWT + RBAC
    - Spring Mail + Scheduler
    - Modules : `certificat`, `device`, `organisation`, `marche`, `audit`

2. **Frontend** (React 18, Vite, TypeScript)

    - Structure feature-based sous `src/features/`
    - UI library : Tailwind+Headless UI
    - State : React-Query pour data + Context pour auth

3. **DevOps & QA**

    - Dockerfile & Compose (dev/test)
    - CI (GitHub Actions) : `build`, `test`, `lint`, `deploy`
    - Tests : JUnit + Testcontainers, React Testing Library, Cypress

## 5. Roadmap (Phases)

### Phase 1 : Setup & Auth

-

### Phase 2 : Gestion Certificats

-

### Phase 3 : Gestion Organisations & Devices

-

### Phase 4 : Gestion Marchés Terminés

-

### Phase 5 : Audit & Security Hardening

-

### Phase 6 : Release & Monitoring

-

## 6. Livrables et Critères de Succès

- Code source hébergé sur Git.
- CI filière verte (build/tests).
- Docs Swagger & guides Postman.
- Report tests (> 80 % coverage).
- Démo fonctionnelle & retours utilisateurs.

---

**Pour toute question ou clarification**, contactez l’Architecte Technique ou le Product Owner.

