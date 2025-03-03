# Introdução

Engenharia de Software (ES) é um domínio vasto, no qual ontologias são instrumentos úteis para lidar com problemas relacionados à semântica e gestão do conhecimento. Quando ontologias de ES são construídas e utilizadas isoladamente, alguns problemas permanecem, em particular aqueles relacionados com integração do conhecimento.

A proposta de SEON (Software Engineering Ontology Network) consiste em organizar as ontologias de ES em uma rede de ontologias que suporte a criação, integração e evolução das suas ontologias. Uma rede de ontologias é um conjunto de ontologias relacionadas entre si através de uma variedade de relações, tais como alinhamento e dependência. Uma ontologia em rede, por sua vez, é uma ontologia incluída em tal rede, compartilhando conceitos e relações com outras ontologias.

A criação da rede SEON é importante, pois ontologias como Medição de Software, Processo de Software, Requisitos, Gerência de Configuração, Gerência de Projetos de Software e Teste de Software podem nela compartilhar conceitos e relações.

Logo, SEON é uma rede de ontologias em Engenharia de Software que fornece um conjunto bem fundamentado de ontologias de referência em ES e mecanismos para construir e integrar novas ontologias na rede.

# Objetivo do Projeto
Sendo uma rede de ontologias, SEON é como um organismo vivo e está em constante evolução. Requer um esforço contínuo e a longo prazo, com ontologias sendo adicionadas e integradas de maneira incremental e gradativa.

Portanto, neste repositório temos o objetivo de criar um script que leia arquivos .asta e crie um site que mostre a rede de ontologias SEON.

# Requisitos

* Java 21
* IDE: IntelliJ IDEA
* Linux

# Execução do Código

## Passos

1. Clonar o repositório com o comando:
```
git clone https://github.com/naascimento0/SEON_Script_Website.git
```
2. Colocar o arquivo Astah (.asta) na raiz do projeto com o nome "astah_seon.asta"

3. Abrir o projeto com a IDE IntelliJ;

4. Em File/Project Structure, escolher a versão do Java 21;

5. Executar o arquivo Parser.java localizado em src/main/java/nemo/seon/parser/Parser.java;

6. Terminada a execução, abrir o arquivo HomePage.html localizado em pages/HomePage.html.


# Estrutura do Projeto

O projeto está dividido em três pacotes:
* model: contém as classes que representam o modelo de dados;
* parser: contém as classes que fazem a leitura do arquivo .asta e criam as instâncias das classes do modelo;
* writer: contém as classes que escrevem os arquivos HTML.

# Classes do Modelo
## Package
O Package é uma classe que guarda atributos de uma instância da classe IPackage no Astah. Toda pasta no arquivo asta é um pacote.

Atributos da classe Package:
- String name: é o nome do pacote no Astah;
- String definition: é a definição do pacote no Astah;
- PackType type: tipo enumerado que determina o tipo de pacote. O valor desse tipo é determinado por um Tagged Value. O desenvolvedor de ontologias determina o valor de Tagged Value de um pacote dentro do Astah;
- int order: (desconhecido);
- IPackage pack: é objeto que representa a própria instância do IPackage;
- List<Package\> subpacks: são os pacotes que estão no subdiretório de um pacote, ou seja, os pacotes filhos em um nível abaixo do pacote pai;
- Package parent: é o pacote pai;
- Map<IPackage, Package\> packageMap: objeto estático que mapeia um IPackage para um Package. Ele é usado para mapear um IPackage para um Package, para que seja possível acessar um Package a partir de um IPackage;
- List<Diagram\> diagrams: são os diagramas que estão no pacote;
- List<Dependency\> dependencies: são as dependências que o pacote tem com outros pacotes.

## Ontology
O Ontology é uma classe que herda de Package, ela precisa existir pois armazenará Concepts. Um pacote é dado como Ontology se ele tiver o Tagged Value "Ontology ou Subontology"

Além dos atributos de pacote, ela possui:
- String fullName: nome completo da ontologia;
- String shortName: nome abreviado da ontologia;
- List<Concept\> concepts: classes que compõem a ontologia.

OBS: O IPackage pack de Ontology é um Package, visto que a ontologia é uma pasta no arquivo.
OBS: o fullName e o shortName são Tagged Values que o desenvolvedor define no Astah

## Concept
O Concept é uma classe que representa as classes dentro de uma ontologia. Por exemplo, a ontologia SysSwO tem os conceitos (classes no arquivo Astah) Complex Computer System, Computer System, Controller, dentre outros.

Atributos da classe Concept:
- String name: nome do conceito;
- String definition: definição do conceito;
- String stereotype: um valor que o desenvolvedor determina no Astah (toda classe pode ter um Stereotype no Astah), valores podem ser 2ndOT, kind, role, mode, subkind, event, phase, category, rolemixin, dentre outros;
- Ontology ontology: é a ontologia a qual o conceito pertence;
- IClass object: é o objeto que representa a classe no Astah;
- Map<IClass, Concept> conceptMap: objeto estático que mapeia um IClass para um Concept. Ele é usado para mapear um IClass para um Concept, para que seja possível acessar um Concept a partir de um IClass;
- List<Concept>	generalizations: são as generalizações que o conceito tem com outros conceitos https://www.ibm.com/docs/en/dma?topic=diagrams-generalization-relationships).

## Dependency
A classe Dependency sinaliza uma relação de dependência entre dois pacotes.

Atributos da classe Dependency:
- Package source: pacote que recebe as dependências, ou seja, o pacote origem
- Package target: pacote que é o cliente, ou seja, faz parte do conjunto de dependências do Package source
- String definition: definição justificando porque ocorre uma relação de dependência
- String level: um Tagged Value escolhido pelo desenvolvedor Astah.

## Relation
A classe Relation representa a relação entre dois conceitos (duas classes do Astah).

Atributos da classe Relation:
- String name: nome da relação;
- String definition: (não encontrei nenhuma relação que tenha definition não vazio);
- String stereotype: atributo "stereotype" de uma classe no Astah, definida pelo desenvolvedor Astah;
- boolean composition: valor para informar se a relação é de Composição (https://www.uml-diagrams.org/composition.html);
- Package pack: (não sei porque existe isso, dado que em todas as instâncias da classe o Package pack é nulo);
- Concept source: qual é a classe do Astah na origem da relação;
- Concept target: qual é a classe do Astah no destino da relação;
- String smult: multiplicidade do source;
- String tmult: multiplicidade do target;
- List<Relation\> relationsList: lista estática que armazena todas as relações.

## Diagram
A classe Diagram representa os diagramas no arquivo Astah
Atributos da classe Diagram:
- String name: nome do diagrama;
- String definition: definição do diagrama;
- DiagType type: Tagged Value que pode representar os valores PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE;
- String network: Tagged Value que diz se pertence o diagrama pertence a SEON, HCI-ON, entre outros;
- pack: pacote cujo diagrama está localizado;
- IDiagram object: objeto que representa o diagrama no Astah.


# Considerações
É necessário o Astah Professional para colocar valores de Tagged Value em pacotes e classes. O Astah Community e Astah UML não permitem isso.
