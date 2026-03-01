# Introdução

Engenharia de Software (ES) é um domínio vasto, no qual ontologias são instrumentos úteis para lidar com problemas relacionados à semântica e gestão do conhecimento. Quando ontologias de ES são construídas e utilizadas isoladamente, alguns problemas permanecem, em particular aqueles relacionados com integração do conhecimento.

A proposta de SEON (Software Engineering Ontology Network) consiste em organizar as ontologias de ES em uma rede de ontologias que suporte a criação, integração e evolução das suas ontologias. Uma rede de ontologias é um conjunto de ontologias relacionadas entre si através de uma variedade de relações, tais como alinhamento e dependência. Uma ontologia em rede, por sua vez, é uma ontologia incluída em tal rede, compartilhando conceitos e relações com outras ontologias.

A criação da rede SEON é importante, pois ontologias como Medição de Software, Processo de Software, Requisitos, Gerência de Configuração, Gerência de Projetos de Software e Teste de Software podem nela compartilhar conceitos e relações.

Logo, SEON é uma rede de ontologias em Engenharia de Software que fornece um conjunto bem fundamentado de ontologias de referência em ES e mecanismos para construir e integrar novas ontologias na rede.

# Objetivo do Projeto
Sendo uma rede de ontologias, SEON é como um organismo vivo e está em constante evolução. Requer um esforço contínuo e a longo prazo, com ontologias sendo adicionadas e integradas de maneira incremental e gradativa.

Este repositório evoluiu o script de geração do site de SEON criado pelo professor Fabiano Ruy para uma aplicação web Spring Boot completa, oferecendo navegação dinâmica, templates reutilizáveis e interface responsiva para explorar a rede SEON.

# Requisitos

* Java 21
* Gradle (incluído via wrapper)
* Navegador web moderno
* (Opcional) IDE: IntelliJ IDEA para desenvolvimento

# Execução

1. Clonar o repositório:
```bash
git clone https://github.com/naascimento0/SEON_Script_Website.git
cd SEON_Script_Website
```

2. Configurar variáveis de ambiente para autenticação:
```bash
cp .env.example .env
# Editar .env com suas credenciais seguras
```

3. Colocar o arquivo Astah (`.asta`) na raiz do projeto com o nome `astah_seon.asta`.

4. Executar a aplicação:
```bash
./gradlew bootRun
```

5. Acessar no navegador: [http://localhost:8080](http://localhost:8080)

> Na inicialização, a aplicação parseia o arquivo `.asta` via Astah API e exporta automaticamente todos os diagramas UML como imagens PNG para `src/main/resources/static/images/`.

# Estrutura do Projeto

```
SEON_Script_Website/
├── src/main/
│   ├── java/nemo/seon/
│   │   ├── config/                # Configurações (Security, StartupDiagramGenerator)
│   │   ├── controller/            # Controllers Spring MVC
│   │   ├── model/                 # Entidades do domínio (Ontology, Concept, Relation…)
│   │   │   └── dto/               # Records de apresentação (SectionView, ConceptRow…)
│   │   ├── parser/                # ModelReader — leitura de arquivos .asta
│   │   └── service/               # Lógica de negócio
│   │       ├── OntologyService        # Carrega e registra ontologias no SeonRegistry
│   │       ├── OntologyViewService    # Monta DTOs para renderização Thymeleaf
│   │       └── DiagramsService        # Exporta diagramas via Astah API
│   └── resources/
│       ├── templates/
│       │   ├── fragments/         # Fragmentos reutilizáveis (navbar, footer, ontology)
│       │   ├── TemplateHomePage.html
│       │   ├── TemplateOntologyPage.html
│       │   ├── TemplatePublications.html
│       │   ├── UploadPage.html
│       │   ├── LoginPage.html
│       │   └── ErrorPage.html
│       └── static/
│           ├── css/seon-theme.css  # CSS customizado do projeto
│           ├── css/bootstrap*.css  # Bootstrap 5.3.3 (local)
│           └── images/             # Diagramas gerados (gitignored, exceto estáticos)
├── jars/                          # JARs proprietários do Astah API
├── build.gradle
├── .env.example                   # Template de variáveis de ambiente
├── astah_seon.asta                # Arquivo Astah principal (gitignored)
└── README.md
```

## Fluxo de Funcionamento

1. **Startup**: `StartupDiagramGenerator` invoca `DiagramsService` para exportar diagramas PNG do `.asta`
2. **Parsing**: `ModelReader` lê o `.asta` e popula o `SeonRegistry` com ontologias, conceitos e relações
3. **Requisição**: Usuário acessa endpoint (ex.: `/ontology/SPO`)
4. **Controller**: `PageController` recebe a requisição
5. **View Service**: `OntologyViewService` monta DTOs (`SectionView`, `ConceptRow`, `DiagramView`…)
6. **Template**: Thymeleaf renderiza o HTML usando os DTOs e fragmentos reutilizáveis
7. **Response**: Página completa enviada ao navegador

## Templates e Fragmentos

| Template | Descrição |
|---|---|
| `TemplateHomePage.html` | Página inicial com definição, arquitetura e visão da rede SEON |
| `TemplateOntologyPage.html` | Template reutilizável para qualquer ontologia individual |
| `TemplatePublications.html` | Publicações acadêmicas da SEON |
| `UploadPage.html` | Upload de arquivos `.asta` (requer autenticação ADMIN) |
| `LoginPage.html` | Tela de login |
| `ErrorPage.html` | Página de erro genérica |

**Fragmentos** (`fragments/`):
- `layout.html` — Navbar com navegação dinâmica e footer
- `ontology.html` — Fragmentos recursivos para renderizar seções, diagramas e tabelas de conceitos

## Tecnologias

| Camada | Tecnologia | Versão |
|---|---|---|
| Framework | Spring Boot | 3.2.5 |
| Templates | Thymeleaf | (gerenciado pelo Spring) |
| Segurança | Spring Security + BCrypt | (gerenciado pelo Spring) |
| Env | spring-dotenv | 4.0.0 |
| Frontend | Bootstrap (CDN + local) | 5.3.3 |
| Logging | SLF4J + Logback | 2.0.13 / 1.5.6 |
| Upload | Commons FileUpload | 1.5 |
| Diagramas | Astah API (JARs locais) | — |
| Build | Gradle (wrapper) | 8.10 |
| Linguagem | Java | 21 |

## Configuração de Segurança

### Variáveis de Ambiente

A aplicação usa [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) para carregar automaticamente o arquivo `.env`:

```bash
# .env (copiar de .env.example)
SEON_ADMIN_USERNAME=admin
SEON_ADMIN_PASSWORD=your_secure_admin_password
```

### Níveis de Acesso
- **Público**: Home, ontologias, publicações
- **ADMIN**: Upload de arquivos Astah (requer login)

### Configuração Inicial
1. `cp .env.example .env`
2. Editar `.env` com credenciais seguras
3. Nunca versionar o arquivo `.env`