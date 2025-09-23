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

# Execução do Código

## Aplicação Spring Boot (Recomendado)

1. Clonar o repositório:
```bash
git clone https://github.com/naascimento0/SEON_Script_Website.git
```

2. Navegar para o diretório:
```bash
cd SEON_Script_Website
```

3. Configurar variáveis de ambiente para autenticação:
```bash
cp .env.example .env
# Editar .env com suas credenciais seguras
```

4. Colocar o arquivo Astah (.asta) na raiz do projeto com o nome "astah_seon.asta".

5. Executar a aplicação:
```bash
./gradlew bootRun
```

6. Acessar no navegador:
```
http://localhost:8080
```

## Geração Estática (Método Legado)

Para usar o sistema original de páginas estáticas:

1. Colocar arquivo Astah (.asta) na raiz com nome "astah_seon.asta"
2. Abrir projeto no IntelliJ IDEA
3. Configurar Java 21 em File/Project Structure
4. Executar Parser.java em `src/main/java/nemo/seon/parser/Parser.java`
5. Abrir HomePage.html gerado em `pages/HomePage.html`


# Estrutura do Projeto

## Arquitetura Atual (Spring Boot)

O projeto migrou para uma aplicação web Spring Boot com sistema de templates dinâmicos:

```
SEON_Script_Website/
├── src/main/
│   ├── java/                      # Código fonte Java
│   │   └── nemo/seon/             # Pacotes principais
│   │       ├── model/             # Classes do modelo de dados
│   │       ├── parser/            # Leitura de arquivos .asta
│   │       ├── writer/            # Geração de HTML
│   │       ├── controller/        # Controllers Spring
│   │       ├── service/           # Serviços de negócio
│   │       └── config/            # Configurações
│   └── resources/
│       ├── templates/             # Templates Thymeleaf
│       ├── static/                # CSS, JS, imagens
│       └── application.properties # Configurações
├── build.gradle                   # Build e dependências
├── astah_seon.asta               # Arquivo Astah principal
└── README.md                     # Este arquivo
```

## Sistema de Templates

### Templates Principais:

**TemplateHomePage.html**
- Página inicial com visão geral da rede SEON
- Sidebar responsiva com navegação para ontologias
- Renderização dinâmica via Thymeleaf

**TemplateOntologyPage.html**
- Template reutilizável para ontologias individuais
- Geração automática de diagramas e estruturas
- Navegação contextual integrada

**TemplatePublications.html**
- Lista de publicações acadêmicas da SEON
- Organização cronológica e por categoria
- Conteúdo baseado em pesquisas reais

**UploadPage.html / LoginPage.html**
- Interface de upload para arquivos Astah
- Sistema de autenticação básico

### Funcionamento:

1. **Requisição**: Usuario acessa endpoint como `/ontology/SPO`
2. **Controller**: PageController processa requisição
3. **Service**: OntologyService carrega dados da ontologia
4. **Template**: Thymeleaf renderiza HTML com dados dinâmicos
5. **Response**: Página completa enviada ao navegador

### Recursos:

- **Navegação Dinâmica**: Sidebar automática com todas ontologias
- **Responsividade**: Layout adaptativo via Bootstrap 5.3.3
- **Geração Automática**: Diagramas UML convertidos de arquivos Astah
- **Figuras Numeradas**: Sistema automático de numeração por ontologia

## Arquitetura Legada (Estática)

O sistema original gerava páginas HTML estáticas:

* **model**: Classes que representam o modelo de dados das ontologias
* **parser**: Classes para leitura de arquivos .asta, criação de instâncias e escrita dos arquivos HTML

## Tecnologias Utilizadas

### Backend
- **Spring Boot**: Framework web principal
- **Thymeleaf**: Engine de templates dinâmicos  
- **Java 21**: Linguagem de programação
- **Gradle**: Gerenciamento de dependências

### Frontend
- **Bootstrap 5.3.3**: Framework CSS responsivo
- **HTML5/CSS3**: Estrutura e estilização
- **JavaScript**: Funcionalidades interativas

### Processamento
- **Astah API**: Leitura e conversão de diagramas UML
- **Apache Commons**: Utilitários para manipulação de arquivos

## Funcionalidades

### Navegação Integrada
- Homepage com visão geral da rede SEON
- Acesso direto a qualquer ontologia via sidebar
- Navegação breadcrumb e links contextuais

### Visualização de Ontologias
- Diagramas UML gerados automaticamente
- Estruturas de classes organizadas hierarquicamente
- Descrições detalhadas de cada ontologia

### Publicações Acadêmicas
- Lista completa de trabalhos relacionados à SEON
- Organização cronológica e por categoria
- Referencias formatadas em padrão acadêmico

### Sistema de Upload
- Interface para adição de novos arquivos Astah
- Processamento automático de diagramas
- Integração com sistema de ontologias existente

## Configuração de Segurança

### Variáveis de Ambiente
A aplicação utiliza variáveis de ambiente para configurar credenciais de usuários:

```bash
# No arquivo .env
SEON_ADMIN_USERNAME=admin
SEON_ADMIN_PASSWORD=your_secure_admin_password
```

### Níveis de Acesso
- **Público**: Acesso livre a páginas principais (home, ontologias, publicações)
- **ADMIN**: Acesso à funcionalidade de upload de arquivos Astah

### Configuração Inicial
1. Copiar arquivo de exemplo: `cp .env.example .env`
2. Editar `.env` com credenciais seguras
3. Nunca versionar o arquivo `.env` no controle de versão
4. Usar senhas fortes para ambiente de produção