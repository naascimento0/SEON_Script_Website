# SEON - Módulos da Aplicação

Esta pasta contém a implementação principal da aplicação Spring Boot. A arquitetura está organizada em módulos especializados que trabalham em conjunto para processar arquivos Astah e gerar uma interface web dinâmica.

## Estrutura dos Módulos

```
src/main/java/nemo/seon/
├── SeonBackendApplication.java    # Classe principal Spring Boot
├── WebConfig.java                 # Configuração de recursos estáticos
├── config/                        # Configurações da aplicação
├── controller/                    # Controllers para endpoints web
├── model/                         # Modelo de dados (tem README próprio)
├── parser/                        # Processamento de arquivos Astah
├── service/                       # Lógica de negócio
└── writer/                        # Geração de conteúdo HTML
```

## Descrição dos Módulos

### config/
Configurações centrais da aplicação Spring Boot.

**SecurityConfig.java**
- Configuração de segurança da aplicação
- Define permissões de acesso para diferentes endpoints
- Controla autenticação para área de upload

**StartupDiagramGenerator.java**
- Componente executado na inicialização da aplicação
- Processa arquivo Astah principal automaticamente
- Gera diagramas iniciais se disponíveis

### controller/
Controllers Spring MVC que gerenciam as requisições HTTP.

**PageController.java**
- Controller principal para páginas de conteúdo
- Gerencia endpoints: home, ontologias individuais, publicações
- Integra dados dos services com templates Thymeleaf

**AstaController.java**
- Controller específico para processamento de arquivos Astah
- Gerencia upload e processamento de novos arquivos
- Interface com parser para geração de ontologias

**AuthController.java**
- Controller para autenticação e autorização
- Gerencia login e controle de acesso
- Integração com sistema de segurança

### parser/
Módulo responsável por processar arquivos Astah (.asta) e extrair informações das ontologias.

**Parser.java**
- Classe principal do módulo de parsing
- Coordena a leitura e processamento de arquivos Astah
- Integração entre ModelReader e sistema de geração

**ModelReader.java**
- Leitor específico para arquivos Astah
- Extrai informações de diagramas UML
- Converte estruturas Astah para modelo interno

### service/
Camada de serviços com lógica de negócio da aplicação.

**OntologyService.java**
- Serviço central para gerenciamento de ontologias
- Fornece informações sobre ontologias disponíveis
- Integração entre dados estáticos e dinâmicos

**DiagramsService.java**
- Serviço especializado em processamento de diagramas
- Gerencia geração e cache de imagens
- Coordena numeração de figuras por ontologia

### writer/
Módulo responsável pela geração de conteúdo HTML e processamento de saída.

**OntologiesWriter.java**
- Gerador principal de conteúdo HTML para ontologias
- Converte modelo de dados em estruturas HTML
- Integração com sistema de templates

**PageWriter.java**
- Escritor de páginas estáticas (método legado)
- Mantido para compatibilidade com sistema anterior
- Geração de arquivos HTML independentes

**Utils.java**
- Utilitários compartilhados pelos módulos de escrita
- Funções auxiliares para formatação e processamento
- Helpers para geração de HTML e manipulação de strings

## Fluxo de Processamento

### 1. Inicialização
- `SeonBackendApplication.java` inicia aplicação Spring Boot
- `WebConfig.java` configura recursos estáticos
- `StartupDiagramGenerator.java` processa arquivos Astah iniciais

### 2. Processamento de Arquivos
- `Parser.java` coordena leitura de arquivos .asta
- `ModelReader.java` extrai informações dos diagramas
- Dados são convertidos para estruturas do módulo `model/`

### 3. Geração de Conteúdo
- `DiagramsService.java` processa imagens e diagramas
- `OntologiesWriter.java` gera conteúdo HTML específico
- Templates Thymeleaf renderizam páginas finais

### 4. Servir Conteúdo Web
- `PageController.java` gerencia requisições de páginas
- `OntologyService.java` fornece dados das ontologias
- Templates recebem dados dinâmicos via controllers

## Integração entre Módulos

### Parser → Model → Service
1. Parser processa arquivos Astah
2. Model armazena estruturas de dados
3. Service fornece acesso organizado aos dados

### Service → Writer → Controller
1. Service prepara dados de negócio
2. Writer gera conteúdo HTML específico
3. Controller integra com templates para resposta final

### Config → Toda Aplicação
1. SecurityConfig define políticas de acesso
2. WebConfig configura recursos estáticos
3. StartupDiagramGenerator prepara ambiente inicial

## Desenvolvimento e Manutenção

### Adicionando Nova Ontologia
1. Processar arquivo .asta via Parser
2. Service automaticamente detecta nova ontologia
3. Controller e templates se adaptam automaticamente

### Modificando Processamento
1. **Parser/ModelReader**: Para mudanças na leitura de Astah
2. **Writer**: Para alterações na geração de HTML
3. **Service**: Para modificar lógica de negócio

### Customizando Interface
1. **Controller**: Para novos endpoints ou dados
2. **Service**: Para nova lógica de apresentação
3. **Config**: Para novas configurações de segurança/recursos

## Tecnologias por Módulo

- **Config/Controller**: Spring Boot, Spring Security
- **Parser**: Astah API, Apache Commons
- **Service**: Spring Core, Java Collections
- **Writer**: StringBuilder, Template processing

Cada módulo tem responsabilidade específica mas trabalha de forma integrada para fornecer a experiência completa da aplicação SEON.
