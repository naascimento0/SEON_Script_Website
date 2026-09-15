# SEON — Software Engineering Ontology Network

SEON is a network of software engineering ontologies designed to support the creation, integration, and evolution of ontologies in the Software Engineering domain. Rather than treating ontologies as isolated artifacts, SEON organizes them into a structured network where ontologies can share concepts and relations — covering areas such as Software Measurement, Software Process, Requirements, Configuration Management, Project Management, and Software Testing.

This repository contains the **SEON web application**: a Spring Boot REST API that parses Astah `.asta` model files, exports UML diagrams as PNGs, and serves an interactive React + Chakra UI single-page application for browsing the ontology network.

---

## Features

- Browse all SEON ontologies with full descriptions, UML diagrams, and concept definitions
- Clickable image maps linking diagram elements to their concept detail pages
- Academic publications organized by ontology and topic
- Admin upload of new `.asta` files to refresh content at runtime
- Single-page React frontend with client-side routing

---

## Requirements

- Java 21
- Node 20+ (for the frontend build)
- Gradle (included via wrapper — no installation needed)
- An Astah `.asta` model file placed at the project root

---

## Setup & Running

**1. Clone the repository**
```bash
git clone https://github.com/naascimento0/SEON_Script_Website.git
cd SEON_Script_Website
```

**2. Configure environment variables**
```bash
cp .env.example .env
# Edit .env and set secure credentials
```

| Variable | Default | Description |
|---|---|---|
| `SEON_ADMIN_USERNAME` | `admin` | Admin login username |
| `SEON_ADMIN_PASSWORD` | `admin` | Admin login password |
| `SEON_ASTAH_FILEPATH` | `astah_seon.asta` | Path to the `.asta` model file |
| `SEON_ASTAH_SCRIPT` | `jars/astah-command.sh` | Astah CLI script for PNG export |
| `SEON_IMAGES_OUTPUT` | `src/main/resources/static/images` | Output directory for exported PNGs |

**3. Place the Astah file**

Put the SEON `.asta` file at the path defined by `SEON_ASTAH_FILEPATH` (default: `astah_seon.asta` at the project root).

**4. Run the application**

Single-process mode (Gradle builds the React bundle and Spring Boot serves everything):
```bash
./gradlew bootRun
```
Open [http://localhost:8080](http://localhost:8080).

Dev mode (hot reload for the frontend):
```bash
# Terminal 1
./gradlew bootRun

# Terminal 2
cd frontend
npm run dev
```
Open [http://localhost:5173](http://localhost:5173) — Vite proxies API calls to `:8080`.

> On startup, the application automatically parses the `.asta` file via the Astah API and exports all UML diagrams as PNG images to the configured output directory.

---

## Common Commands

```bash
./gradlew bootRun         # Start the application (Tomcat on port 8080)
./gradlew build           # Compile, build the React bundle, package as JAR
./gradlew test            # Run tests
./gradlew clean           # Clean build artifacts
./gradlew frontendBuild   # Build only the React app
cd frontend && npm run dev    # Vite dev server with HMR
cd frontend && npm run build  # Production bundle into frontend/dist
```

---

## Project Structure

```
SEON_Script_Website/
├── src/main/
│   ├── java/nemo/seon/
│   │   ├── config/                # SecurityConfig, StartupDiagramGenerator
│   │   ├── controller/            # ApiController, AstaController, OwlController, SpaController
│   │   ├── model/                 # Domain entities + DTOs (Ontology, Concept, Relation, …)
│   │   │   └── dto/               # JSON records: OntologyPageResponse, ConceptRow, DiagramView, …
│   │   ├── parser/                # ModelReader — reads .asta via Astah API
│   │   └── service/               # OntologyService, OntologyViewService, DiagramsService, OwlService
│   └── resources/
│       ├── application.properties
│       └── static/
│           └── images/            # Exported diagrams (gitignored except a few fixed assets)
├── frontend/                      # React + Vite + TypeScript SPA
│   ├── src/
│   │   ├── api/                   # axios client + React Query hooks
│   │   ├── components/            # Layout, Section, DiagramWithMap, HtmlContent
│   │   ├── pages/                 # Home, Publications, Login, Upload, Ontology, NotFound
│   │   ├── data/                  # Static datasets (publications list)
│   │   ├── types/                 # TypeScript mirrors of the Java DTOs
│   │   ├── App.tsx                # React Router routes
│   │   └── main.tsx               # Entry: ChakraProvider + QueryClient + BrowserRouter
│   ├── vite.config.ts             # Dev proxy to :8080
│   └── package.json
├── jars/                          # Vendored Astah API JARs
├── build.gradle                   # Includes npmInstall + frontendBuild tasks
├── .env.example
└── README.md
```

---

## Architecture

**No database.** All data is in-memory: ontologies are loaded from the `.asta` file at startup and cached in `SeonRegistry`. Credentials are managed via Spring Security's `InMemoryUserDetailsManager`.

The backend is a pure REST API; the React SPA renders the UI. In production the SPA is bundled into the JAR's `static/` and served by Spring Boot. `SpaController` forwards known SPA paths (`/`, `/publications`, `/login`, `/upload`, `/ontology/{name}`) to `/index.html` so React Router can take over.

### Request flow

```
GET /ontology/{name}                  ← React Router (client-side)
  → useOntology()                     ← React Query
    → GET /api/ontologies/{name}      ← ApiController
      → OntologyService.findByName()  ← in-memory cache
      → OntologyViewService           ← entity → DTOs
      → JSON                          ← Jackson
    → OntologyPage.tsx                ← Chakra UI components
```

### Upload flow

```
POST /upload-asta  (ROLE_ADMIN only, X-XSRF-TOKEN required)
  → AstaController
  → saves .asta file
  → DiagramsService                   (re-exports PNGs via astah-command.sh)
  → OntologyService.reload()          (re-parses .asta, rebuilds cache)
```

### Auth flow

```
POST /login   (form-urlencoded username/password, CSRF header)
  → 200 + { username, roles }, sets JSESSIONID
  → 401 + { error: "bad_credentials" } on failure

GET /api/auth/me
  → 200 + { username, roles } if authenticated
  → 401 + { error: "unauthorized" } otherwise

POST /logout  (CSRF header)
  → 200 + { status: "ok" }
```

### Startup flow

```
Application start
  → StartupDiagramGenerator
  → DiagramsService                   (exports all diagrams as PNGs)
  → ModelReader                       (parses .asta, populates SeonRegistry)
```

---

## API

| Method | Path | Description | Access |
|---|---|---|---|
| `GET` | `/api/meta` | Misc metadata (current year, …) | Public |
| `GET` | `/api/ontologies` | List of ontologies | Public |
| `GET` | `/api/ontologies/{name}` | Full ontology page payload | Public |
| `GET` | `/api/auth/me` | Current user | Public (401 if anon) |
| `POST` | `/login` | Form-urlencoded login | Public |
| `POST` | `/logout` | Logout | Public |
| `POST` | `/upload-asta` | Upload a new `.asta` file | ADMIN |
| `GET` | `/seon.owl` | Download generated OWL file | Public |

CSRF is enabled via cookie `XSRF-TOKEN` / header `X-XSRF-TOKEN`.

---

## Technology Stack

### Backend
| Layer | Technology | Version |
|---|---|---|
| Framework | Spring Boot | 3.2.5 |
| Security | Spring Security + BCrypt | (managed by Spring) |
| Environment | spring-dotenv | 4.0.0 |
| Logging | SLF4J + Logback | 2.0.13 / 1.5.6 |
| File upload | Commons FileUpload | 1.5 |
| Diagrams | Astah API (vendored JARs) | — |
| OWL export | OWL API | 5.5.0 |
| Build | Gradle (wrapper) | 8.10 |
| Language | Java | 21 |

### Frontend
| Layer | Technology | Version |
|---|---|---|
| Framework | React | 19 |
| Language | TypeScript | 6 |
| Bundler | Vite | 8 |
| UI library | Chakra UI | 3 |
| Routing | React Router | 7 |
| Data fetching | TanStack Query | 5 |
| HTTP | axios | 1 |

---

## Security

| Route | Access |
|---|---|
| `/`, `/publications`, `/upload`, `/login`, `/ontology/**` | Public (SPA fallback) |
| `/api/ontologies/**`, `/api/meta`, `/api/auth/**` | Public |
| `/seon.owl`, `/images/**`, `/assets/**` | Public |
| `/upload-asta` | ADMIN only |

Credentials are loaded from the `.env` file via spring-dotenv. Never commit the `.env` file — use `.env.example` as a template.
