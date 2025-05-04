## 1. Objectif du Projet

Concevoir une application web métier pour :

- **Suivi & Alerte** : Certificats SSL/TLS (acheté et généré) avec alertes email.
- **Gestion Equipment** : Inventaire des devices (firewalls) par site, ports, versions, garantie.
- **Gestion Marchés** : Suivi des marchés liés aux devices.

## 2. Responsabilités

- **Back-end** : Implémente les API Spring Boot (certificats, devices, marchés, alertes).
- **Front-end** : Crée l’application React/Vite (UI, appels API, gestion d’état).

## 3. Périmètre et Fonctions Clés

| Fonction                   | Description                                    | Priorité |
|----------------------------|------------------------------------------------| -------- |
| Import certificats générés | Charger depuis DB pour suivi & alerte          | Haute    |
| CRUD certificats achetés   | Enregistrer, modifier, lister certifs externes | Haute    |
| Alerte email 30j avant exp. | !!!!!                                          | Haute    |
| CRUD Sites & Devices       | Site, device, ports, garantie...               | Moyenne  |
| Gestion Marchés    | Filtrer, lister, associer devices              | Moyenne  |
| Audit Log                  | Traçabilité des actions critiques              | Basse    |

## 4. Architecture Technique

1. **Backend** (Spring Boot 3.4.4, Java 21)

    - Spring Data JPA, SQL Server
    - Spring Security JWT
    - (Spring Mail + Scheduler) !!
    - Modules : `certificat`, `device`, `sites`, `marche`, `audit`

2. **Frontend** (React 19, Vite, TypeScript)

    - 
    - 
    -

## 5. Roadmap (Phases)

### Phase 1 : Setup & Auth

-

### Phase 2 : Gestion Certificats

-

### Phase 3 : Gestion Sites & Devices

-

### Phase 4 : Gestion Marchés

-

### Phase 5 : Audit & Security Hardening

-

### Phase 6 : Release & Monitoring

-

---

