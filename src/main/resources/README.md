
# Resources - Sistema de Templates SEON

Esta pasta contém todos os recursos necessários para a aplicação Spring Boot da rede SEON, incluindo templates Thymeleaf, recursos estáticos e configurações.

## Estrutura de Diretórios

```
src/main/resources/
├── templates/              # Templates Thymeleaf
├── static/                 # Recursos estáticos (CSS, JS, imagens)
└── application.properties  # Configurações da aplicação
```

## Sistema de Templates

Os templates utilizam Thymeleaf para renderização dinâmica de conteúdo. Cada template tem uma função específica na aplicação.

### Templates Disponíveis:

**TemplateHomePage.html**
- Página inicial com visão geral da rede SEON
- Contém sidebar com navegação para todas as ontologias
- Exibe definições e estrutura geral da rede
- Renderiza conteúdo através de variáveis Thymeleaf

**TemplateOntologyPage.html**  
- Template reutilizável para qualquer ontologia individual
- Recebe dados dinâmicos: nome, descrição, diagramas
- Gera automaticamente estrutura de classes e propriedades
- Inclui navegação contextual e figuras numeradas

**TemplatePublications.html**
- Lista completa de publicações acadêmicas relacionadas à SEON
- Organizada cronologicamente com formatação acadêmica
- Inclui categorização por área de pesquisa

**UploadPage.html**
- Interface para upload de novos arquivos Astah
- Formulário integrado com processamento backend

**LoginPage.html / ErrorPage.html**
- Páginas de autenticação e tratamento de erros

### Como Funcionam os Templates:

**1. Variáveis Thymeleaf**
Os templates recebem dados através de variáveis definidas no PageController:
```java
model.addAttribute("ontologyName", ontologyName);
model.addAttribute("ontologyDescription", description); 
model.addAttribute("generatedContent", htmlContent);
model.addAttribute("sidebarOntologies", ontologyList);
```

**2. Navegação Dinâmica**
- Sidebar responsiva com lista de ontologias disponíveis
- Menu dropdown para categorias de ontologias  
- Navegação breadcrumb contextual

**3. Geração de Conteúdo**
- Diagramas UML convertidos de arquivos Astah
- Estruturas de classes organizadas hierarquicamente
- Figuras numeradas sequencialmente por ontologia

**4. Responsividade**
- Design adaptativo usando Bootstrap 5.3.3
- Layout otimizado para desktop, tablet e mobile
- Sidebar colapsível em telas menores

### Fluxo de Renderização:

1. **Requisição**: Usuário acessa URL como `/ontology/SPO`
2. **Controller**: PageController processa a requisição
3. **Service**: OntologyService carrega dados da ontologia
4. **Writer**: OntologiesWriter gera HTML específico
5. **Template**: Thymeleaf combina template + dados
6. **Response**: HTML final é enviado ao navegador

## Recursos Estáticos

### Configuração
Recursos estáticos são servidos através da configuração no WebConfig:
```java
registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/");
registry.addResourceHandler("/css/**") 
        .addResourceLocations("classpath:/static/css/");
```

### Estrutura Static:
```
static/
├── css/           # Arquivos de estilo personalizados
├── js/            # Scripts JavaScript
└── images/        # Imagens e recursos visuais
```

## Customização e Manutenção

### CSS Personalizado
- Tema principal em tons de verde para identidade SEON
- Classes específicas para elementos ontológicos
- Animações suaves para transições

### Adicionando Novos Templates
1. Criar arquivo .html na pasta templates/
2. Usar sintaxe Thymeleaf para variáveis dinâmicas
3. Adicionar endpoint correspondente no PageController
4. Testar renderização e navegação

### Modificando Templates Existentes
1. Identificar o template correto na pasta templates/
2. Usar variáveis Thymeleaf existentes ou criar novas
3. Manter estrutura HTML válida e acessibilidade
4. Testar responsividade em diferentes tamanhos

### Manutenção
- Templates são facilmente editáveis
- Adição de novas ontologias é automática
- Sistema modular permite expansões futuras
- Recursos estáticos organizados por tipo

## Configurações

### application.properties
Arquivo principal de configurações da aplicação Spring Boot, incluindo:
- Configurações de servidor
- Configurações de template engine
- Configurações de recursos estáticos