# Classes do Modelo

## Visão Geral

```
model/
├── Package.java       # Pacote Astah (classe base)
├── Ontology.java      # Ontologia (extends Package)
├── Concept.java       # Conceito dentro de uma ontologia
├── Relation.java      # Relação entre dois conceitos
├── Dependency.java    # Dependência entre dois pacotes
├── Diagram.java       # Diagrama no arquivo Astah
├── SeonRegistry.java  # Registro central de Concepts e Packages
└── dto/               # Records usados para renderização Thymeleaf
    ├── ConceptDetail.java
    ├── ConceptRow.java
    ├── DependencyView.java
    ├── DiagramView.java
    ├── MapArea.java
    └── SectionView.java
```

---

## Package
O Package é uma classe que guarda atributos de uma instância da classe IPackage no Astah. Toda pasta no arquivo `.asta` é um pacote.

Atributos da classe Package:
- `String name`: nome do pacote no Astah;
- `PackType type`: tipo enumerado (NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, IGNORE), determinado por um Tagged Value definido pelo desenvolvedor no Astah;
- `int order`: ordem de exibição do pacote;
- `IPackage pack`: objeto que representa a própria instância do IPackage no Astah;
- `List<Package> subpacks`: pacotes filhos (subdiretórios);
- `Package parent`: pacote pai;
- `List<Diagram> diagrams`: diagramas contidos no pacote;
- `List<Dependency> dependencies`: dependências que o pacote tem com outros pacotes.

> **Nota:** A definição do pacote é obtida dinamicamente via `pack.getDefinition()`, não é armazenada como campo.

Métodos utilitários relevantes:
- `getMainOntology()`: sobe a hierarquia de pacotes até encontrar a Ontology raiz;
- `getLevel()`: retorna o `OntoLevel` (FOUNDATIONAL, CORE, DOMAIN) baseado no nome do nível;
- `getReference()` / `getLabel()`: geram strings para referência HTML.

## Ontology
A Ontology herda de Package e armazena Concepts. Um pacote é classificado como Ontology se tiver o Tagged Value "Ontology" ou "Subontology".

Além dos atributos herdados de Package:
- `String fullName`: nome completo da ontologia (Tagged Value);
- `String shortName`: nome abreviado da ontologia (Tagged Value);
- `List<Concept> concepts`: conceitos que compõem a ontologia;
- `OntoLevel`: enum interno com valores FOUNDATIONAL(0), CORE(1), DOMAIN(2).

> **Nota:** `fullName` e `shortName` são Tagged Values definidos pelo desenvolvedor no Astah.

## Concept
O Concept representa uma classe UML dentro de uma ontologia. Por exemplo, a ontologia SysSwO tem os conceitos Complex Computer System, Computer System, Controller, dentre outros.

Atributos da classe Concept:
- `String name`: nome do conceito;
- `String definition`: definição do conceito;
- `String stereotype`: estereótipo definido no Astah (ex: kind, role, mode, subkind, event, phase, category, rolemixin, 2ndOT, etc.);
- `String example`: exemplo de uso do conceito;
- `String sourceDefinition`: fonte da definição;
- `Ontology ontology`: ontologia à qual o conceito pertence;
- `IClass object`: objeto que representa a classe no Astah;
- `List<Concept> generalizations`: generalizações do conceito ([referência UML](https://www.ibm.com/docs/en/dma?topic=diagrams-generalization-relationships));
- `List<Relation> relations`: relações nas quais o conceito participa.

Implementa `Comparable<Concept>`, `equals()` e `hashCode()` (baseados em `name` e `ontology`).

## Dependency
A classe Dependency sinaliza uma relação de dependência entre dois pacotes.

Atributos da classe Dependency:
- `Package target`: pacote do qual se depende;
- `String description`: justificativa da dependência;
- `String level`: Tagged Value escolhido pelo desenvolvedor no Astah.

> **Nota:** O `source` (pacote que possui a dependência) não é armazenado na Dependency — a dependência é adicionada à lista `dependencies` do pacote source.

## Relation
A classe Relation representa a relação entre dois conceitos (duas classes UML no Astah).

Atributos da classe Relation:
- `String name`: nome da relação;
- `String definition`: definição da relação;
- `String stereotype`: estereótipo da relação no Astah;
- `boolean composition`: indica se a relação é de [Composição UML](https://www.uml-diagrams.org/composition.html);
- `Concept source`: conceito na origem da relação;
- `Concept target`: conceito no destino da relação;
- `String sourceMult`: multiplicidade do source;
- `String targetMult`: multiplicidade do target.

> **Nota:** Cada Relation é também adicionada à lista `relations` do Concept source, permitindo navegação bidirecional.

## Diagram
A classe Diagram representa os diagramas no arquivo Astah.

Atributos da classe Diagram:
- `String name`: nome do diagrama;
- `String definition`: definição do diagrama;
- `DiagType type`: tipo do diagrama (PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE), determinado por Tagged Value;
- `Package pack`: pacote no qual o diagrama está localizado;
- `IDiagram object`: objeto que representa o diagrama no Astah.

## SeonRegistry
Registro central que armazena todos os objetos parseados do modelo SEON. Substitui os antigos maps estáticos que existiam em Concept e Package, tornando o modelo seguro para recarregamento e acesso concorrente.

Atributos:
- `Map<IClass, Concept> conceptMap`: mapeia objetos IClass do Astah para Concepts (`ConcurrentHashMap`);
- `Map<IPackage, Package> packageMap`: mapeia objetos IPackage do Astah para Packages (`ConcurrentHashMap`).

Operações principais:
- `registerConcept()` / `getConceptByIClass()` / `getConceptByFullName()` / `getAllConcepts()`
- `registerPackage()` / `getPackageByIPackage()` / `getPackageByFullName()`
- `clear()`: limpa todos os dados registrados (chamado antes de recarregar um novo arquivo `.asta`)

## dto/ — Records para Thymeleaf
O subpacote `dto/` contém Java records usados para passar dados estruturados do `OntologyViewService` para os templates Thymeleaf, eliminando a construção de HTML em Java:

- `DependencyView`: linha da tabela de dependências;
- `DiagramView`: diagrama com imagem, image map e descrição;
- `MapArea`: área clicável dentro de um image map;
- `SectionView`: seção recursiva (subpackages com diagramas aninhados);
- `ConceptRow`: linha da tabela de conceitos;
- `ConceptDetail`: card detalhado de um conceito com generalizações e relações.

---

# Considerações
É necessário o Astah Professional para definir Tagged Values em pacotes e classes. O Astah Community e Astah UML não permitem isso.
