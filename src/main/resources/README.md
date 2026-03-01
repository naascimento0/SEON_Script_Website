
# Resources - Sistema de Templates SEON

Esta pasta contém todos os recursos necessários para a aplicação Spring Boot da rede SEON, incluindo templates Thymeleaf, fragmentos reutilizáveis, recursos estáticos e configurações.

## Estrutura de Diretórios

```
src/main/resources/
├── application.properties          # Configurações da aplicação
├── templates/                      # Templates Thymeleaf
│   ├── TemplateHomePage.html       # Página inicial
│   ├── TemplateOntologyPage.html   # Página de ontologia (reutilizável)
│   ├── TemplatePublications.html   # Publicações acadêmicas
│   ├── UploadPage.html             # Upload de arquivos Astah
│   ├── LoginPage.html              # Autenticação
│   ├── ErrorPage.html              # Tratamento de erros
│   └── fragments/                  # Fragmentos Thymeleaf reutilizáveis
│       ├── layout.html             # Navbar e footer compartilhados
│       └── ontology.html           # Diagramas, seções, conceitos
└── static/                         # Recursos estáticos
    ├── css/
    │   ├── seon-theme.css          # Tema personalizado SEON
    │   └── bootstrap*.css          # Bootstrap 5.3.3
    ├── js/
    │   └── bootstrap*.js           # Bootstrap JS
    └── images/                     # Imagens e diagramas gerados
```

## Sistema de Templates

Os templates utilizam Thymeleaf para renderização dinâmica. Os dados são passados como **DTOs (records Java)** pelo `PageController`, eliminando qualquer construção de HTML em Java.

### Templates Disponíveis

**TemplateHomePage.html**
- Página inicial com visão geral da rede SEON
- Sidebar com navegação para todas as ontologias
- Exibe definições e estrutura geral da rede

**TemplateOntologyPage.html**
- Template reutilizável para qualquer ontologia individual
- Recebe DTOs estruturados: `dependencies`, `ontoDiagrams`, `ontoSections`, `conceptRows`, `conceptDetails`
- Usa fragmentos de `ontology.html` para renderizar cada seção
- Inclui navegação contextual e figuras numeradas automaticamente

**TemplatePublications.html**
- Lista de publicações acadêmicas relacionadas à SEON

**UploadPage.html**
- Interface para upload de novos arquivos Astah (.asta)
- Formulário com proteção CSRF via Thymeleaf (`th:action`)

**LoginPage.html / ErrorPage.html**
- Páginas de autenticação e tratamento de erros

### Fragmentos (`fragments/`)

**layout.html**
- `navbar(activePage)` — barra de navegação com destaque na página ativa
- `footer` — rodapé com ano dinâmico

**ontology.html**
- `diagrams(diagrams)` — renderiza lista de diagramas com imagens e image maps
- `sections(sections)` — renderização **recursiva** de seções (subpackages com headings dinâmicos h3–h6)
- `dependenciesBody(deps)` — corpo da tabela de dependências
- `conceptsBody(concepts)` — corpo da tabela de definição de conceitos
- `detailedConcepts(details)` — cards detalhados de conceitos com generalizações e relações

### Variáveis Thymeleaf (Página de Ontologia)

O `PageController.ontologyPage()` passa os seguintes atributos ao modelo:

| Variável | Tipo | Descrição |
|---|---|---|
| `title` | `String` | Nome completo + sigla da ontologia |
| `ontoLevelIcon` | `String` | Ícone Bootstrap (star/star-half/star-fill) |
| `ontoLevelText` | `String` | Texto do nível (Foundational/Core/Domain) |
| `status` | `String` | Status da ontologia (Well-Established, etc.) |
| `description` | `String` | Definição formatada (com `<br/>`) |
| `dependencies` | `List<DependencyView>` | Dependências para tabela |
| `ontoDiagrams` | `List<DiagramView>` | Diagramas da ontologia |
| `ontoSections` | `List<SectionView>` | Seções recursivas dos subpackages |
| `conceptRows` | `List<ConceptRow>` | Linhas da tabela de conceitos |
| `conceptDetails` | `List<ConceptDetail>` | Cards detalhados de conceitos |

### Fluxo de Renderização

1. **Requisição**: Usuário acessa `/ontology/SPO`
2. **Controller**: `PageController` processa a requisição
3. **Service**: `OntologyService` carrega dados da ontologia do `SeonRegistry`
4. **ViewService**: `OntologyViewService` converte os dados do modelo em DTOs estruturados
5. **Template**: Thymeleaf renderiza `TemplateOntologyPage.html` usando os DTOs + fragmentos
6. **Response**: HTML final é enviado ao navegador

## Recursos Estáticos

### CSS
- **`seon-theme.css`** — Tema personalizado SEON (~420 linhas): cores em tons de verde, estilos para sidebar, tabelas de conceitos, cards de detalhamento, badges, e estilos específicos para elementos ontológicos
- **Bootstrap 5.3.3** — Framework CSS completo (arquivos locais)

### JavaScript
- **Bootstrap 5.3.3 bundle** — Inclui Popper.js para dropdowns, tooltips, etc.

### Imagens
- Diagramas gerados automaticamente a partir do arquivo Astah pelo `DiagramsService` + `StartupDiagramGenerator`

### Configuração de Servimento
Recursos estáticos são servidos via `WebConfig`:
```java
registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/");
```

## Configurações

### application.properties
```properties
# Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Segurança (via .env com spring-dotenv)
seon.admin.username=${SEON_ADMIN_USERNAME:admin}
seon.admin.password=${SEON_ADMIN_PASSWORD:admin}

# Caminhos configuráveis
seon.astah.filepath=${SEON_ASTAH_FILEPATH:astah_seon.asta}
seon.astah.script=${SEON_ASTAH_SCRIPT:jars/astah-command.sh}
```

As variáveis de ambiente são carregadas automaticamente do arquivo `.env` na raiz do projeto via a lib `spring-dotenv`.