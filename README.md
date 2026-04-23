# SEON — Software Engineering Ontology Network

SEON is a network of software engineering ontologies designed to support the creation, integration, and evolution of ontologies in the Software Engineering domain. Rather than treating ontologies as isolated artifacts, SEON organizes them into a structured network where ontologies can share concepts and relations — covering areas such as Software Measurement, Software Process, Requirements, Configuration Management, Project Management, and Software Testing.

This repository contains the **SEON web application**: a Spring Boot app that parses Astah `.asta` model files, exports UML diagrams as PNGs, and serves interactive HTML pages for browsing the ontology network.

---

## Features

- Browse all SEON ontologies with full descriptions, UML diagrams, and concept definitions
- Clickable image maps linking diagram elements to their concept detail pages
- PDF export of any ontology page (browser print dialog)
- Academic publications organized by ontology and topic
- Admin upload of new `.asta` files to refresh content at runtime
- Responsive layout with sidebar navigation

---

## Requirements

- Java 21
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
```bash
./gradlew bootRun
```

**5. Open in browser**

[http://localhost:8080](http://localhost:8080)

> On startup, the application automatically parses the `.asta` file via the Astah API and exports all UML diagrams as PNG images to the configured output directory.

---

## Common Commands

```bash
./gradlew bootRun   # Start the application (Tomcat on port 8080)
./gradlew build     # Compile and package
./gradlew test      # Run tests
./gradlew clean     # Clean build artifacts
```

---

## Project Structure

```
SEON_Script_Website/
├── src/main/
│   ├── java/nemo/seon/
│   │   ├── config/          # Security, startup diagram generation
│   │   ├── controller/      # Spring MVC controllers (pages + .asta upload)
│   │   ├── model/           # Domain entities: Ontology, Concept, Relation, Diagram…
│   │   │   └── dto/         # View records: SectionView, ConceptRow, DiagramView…
│   │   ├── parser/          # ModelReader — reads .asta via Astah API
│   │   └── service/         # Business logic
│   │       ├── OntologyService       # Loads and caches ontologies from SeonRegistry
│   │       ├── OntologyViewService   # Builds DTOs for Thymeleaf rendering
│   │       └── DiagramsService       # Runs astah-command.sh to export PNGs
│   └── resources/
│       ├── templates/
│       │   ├── fragments/            # Reusable fragments (navbar, footer, ontology)
│       │   ├── TemplateHomePage.html
│       │   ├── TemplateOntologyPage.html
│       │   ├── TemplatePublications.html
│       │   ├── UploadPage.html
│       │   ├── LoginPage.html
│       │   └── ErrorPage.html
│       └── static/
│           ├── css/seon-theme.css    # Custom theme (includes print/PDF styles)
│           ├── css/bootstrap*.css    # Bootstrap 5.3.3 (local)
│           └── images/               # Exported diagrams (gitignored)
├── jars/                    # Vendored Astah API JARs
├── build.gradle
├── .env.example             # Environment variable template
└── README.md
```

---

## Architecture

**No database.** All data is in-memory: ontologies are loaded from the `.asta` file at startup and cached in `SeonRegistry`. Credentials are managed via Spring Security's `InMemoryUserDetailsManager`.

### Request flow

```
GET /ontology/{name}
  → PageController
  → OntologyService.findByName()     (in-memory cache)
  → OntologyViewService              (entity → DTO)
  → Thymeleaf template               (renders HTML with image maps)
```

### Upload flow

```
POST /upload-asta  (ROLE_ADMIN only)
  → AstaController
  → saves .asta file
  → DiagramsService (re-exports PNGs via astah-command.sh)
  → OntologyService.reload()         (re-parses .asta, rebuilds cache)
```

### Startup flow

```
Application start
  → StartupDiagramGenerator
  → DiagramsService (exports all diagrams as PNGs)
  → ModelReader (parses .asta, populates SeonRegistry)
```

---

## Templates

| Template | Description |
|---|---|
| `TemplateHomePage.html` | Landing page with SEON definition, architecture, and network overview |
| `TemplateOntologyPage.html` | Reusable page for any individual ontology (description, diagrams, concepts, PDF export) |
| `TemplatePublications.html` | Academic publications organized by site ontologies, other ontologies, and general SEON papers |
| `UploadPage.html` | Upload form for `.asta` files (requires ADMIN role) |
| `LoginPage.html` | Login screen |
| `ErrorPage.html` | Generic error page |

**Fragments** (`fragments/`):
- `layout.html` — Navbar and footer, shared across all pages
- `ontology.html` — Recursive fragments for rendering sections, diagrams, and concept tables

---

## Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Framework | Spring Boot | 3.2.5 |
| Templates | Thymeleaf | (managed by Spring) |
| Security | Spring Security + BCrypt | (managed by Spring) |
| Environment | spring-dotenv | 4.0.0 |
| Frontend | Bootstrap (CDN + local) | 5.3.3 |
| Icons | Bootstrap Icons | 1.11.3 |
| Logging | SLF4J + Logback | 2.0.13 / 1.5.6 |
| File upload | Commons FileUpload | 1.5 |
| Diagrams | Astah API (vendored JARs) | — |
| Build | Gradle (wrapper) | 8.10 |
| Language | Java | 21 |

---

## Security

| Route | Access |
|---|---|
| `/`, `/ontology/**`, `/publications` | Public |
| `/login` | Public |
| `/upload-asta` | ADMIN only |

Credentials are loaded from the `.env` file via spring-dotenv. Never commit the `.env` file — use `.env.example` as a template.
