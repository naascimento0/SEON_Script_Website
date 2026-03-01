# 📋 Análise e Melhorias do Projeto SEON Spring Boot

> Gerado em: Março 2026  
> Status: ✅ Todas as 18 melhorias implementadas

---

## ✅ O que está bom

1. **Estrutura geral** — Organização em `model/`, `model/dto/`, `parser/`, `controller/`, `service/`, `config/` segue bem a separação de responsabilidades.
2. **Gradle Wrapper** — `gradlew` com permissão de execução, `gradle-wrapper.jar` presente, Gradle 8.10. `./gradlew bootRun` funciona corretamente.
3. **Spring Security** — Boa configuração com `BCryptPasswordEncoder`, `InMemoryUserDetailsManager`, e proteção do endpoint `/upload-asta`.
4. **`.gitignore`** — Bem feito: ignora `.env`, `jars/`, `astah_seon.asta`, `build/`.
5. **Variáveis de ambiente** — `application.properties` usa `${SEON_ADMIN_USERNAME:admin}` com fallback.

---

## 🔴 Problemas Críticos

### 1. Estado global estático nos Models
- **Arquivos:** `Concept.java`, `Package.java`, `Relation.java`
- **Problema:** Coleções `static` (`conceptMap`, `packageMap`, `relationsList`) são compartilhadas por toda a JVM. Quando `OntologyService.reloadOntologies()` é chamado, os maps estáticos não são limpos, causando vazamento de memória e dados fantasma. Em servidor web, múltiplas threads podem corromper esses maps (nenhum é `ConcurrentHashMap`).
- **Solução:** Mover essas coleções para uma classe de contexto/registry gerenciada pelo Spring (um `@Component` singleton) em vez de campos estáticos.
- [x] Implementado

### 2. CSRF desabilitado — Vulnerabilidade de Segurança
- **Arquivo:** `SecurityConfig.java`
- **Problema:** `.csrf(csrf -> csrf.disable())` — O endpoint `/upload-asta` é POST protegido por autenticação, mas sem CSRF qualquer site malicioso pode submeter o form se o admin estiver logado.
- **Solução:** Habilitar CSRF e incluir o token no formulário de upload via Thymeleaf (`th:action`).
- [x] Implementado

### 3. Senha no `.env.example`
- **Arquivo:** `.env.example`
- **Problema:** Contém `SEON_ADMIN_PASSWORD=teste`. Quem clonar o repo pode usar direto sem trocar.
- **Solução:** Colocar um placeholder como `CHANGE_ME`.
- [x] Implementado

### 4. `EnvLoader` manual vs. Spring Boot
- **Arquivo:** `EnvLoader.java`, `SeonBackendApplication.java`
- **Problema:** Spring Boot já suporta `.env` files via libs dedicadas. O `EnvLoader` usa `System.setProperty()` antes do Spring iniciar, o que funciona, mas é frágil.
- **Solução:** Considerar usar a lib `me.paulschwarz:spring-dotenv` ou `@PropertySource`.
- [x] Implementado

---

## 🟠 Problemas de Design e Boas Práticas Java/Spring

### 5. `OntologiesWriter` não é um Bean Spring
- **Arquivo:** `PageController.java`
- **Problema:** `new OntologiesWriter()` é criado manualmente no controller. Isso impede testes unitários com mocking e viola a inversão de dependência do Spring.
- **Solução:** Tornar `OntologiesWriter` um `@Component` injetado via construtor.
- [x] Implementado

### 6. `OntologiesWriter` tem estado mutável (`figCount`) — Não é thread-safe
- **Arquivo:** `OntologiesWriter.java`
- **Problema:** Se dois requests de ontologias diferentes chegam simultaneamente, compartilham a mesma instância e o `figCount` é corrompido.
- **Solução:** O contador deveria ser variável local passada por parâmetro, ou usar `AtomicInteger` com escopo por request.
- [x] Implementado

### 7. HTML construído manualmente em Java — Anti-padrão
- **Arquivo:** `OntologiesWriter.java`
- **Problema:** Constrói HTML via `StringBuilder` e `String.replace()`. Extremamente frágil, difícil de manter, e vulnerável a XSS (nenhum escape de HTML é feito). Mistura lógica de negócio com apresentação.
- **Solução ideal:** Mover toda essa lógica para o Thymeleaf template usando `th:each`, `th:text`, `th:utext`, etc.
- [x] Implementado — Criados 6 DTOs (records), `OntologyViewService`, fragmento `fragments/ontology.html` com rendering recursivo. `OntologiesWriter.java` e `Utils.java` removidos.

### 8. `System.out.println` em vez de Logger
- **Arquivos:** `OntologyService.java`, `DiagramsService.java`, `OntologiesWriter.java`, `ModelReader.java`, `Parser.java`, `PageWriter.java`, `Utils.java`
- **Problema:** O projeto já tem SLF4J + Logback como dependência mas usa `System.out/err` em quase todos os lugares.
- **Solução:** Usar `private static final Logger log = LoggerFactory.getLogger(NomeDaClasse.class)` em todas as classes.
- [x] Implementado

### 9. `PageWriter` — Código morto/legado
- **Arquivo:** `PageWriter.java`
- **Problema:** Gera páginas estáticas lendo templates com `fileToString` e escrevendo em `./page/`. Isso é o sistema legado que foi substituído pelo Spring Boot + Thymeleaf.
- **Solução:** Remover ou mover para um pacote `legacy/`.
- [x] Implementado

### 10. `Parser.java` — Código morto
- **Arquivo:** `Parser.java`
- **Problema:** Tem um `main()` que é o entry point legado. Com Spring Boot, `SeonBackendApplication` é o entry point. Contém constantes (`PATH`, `astahFilePath`) usadas por outras classes, misturando responsabilidades.
- **Solução:** Refatorar para ser apenas um utilitário sem `main()`, mover constantes para `application.properties`.
- [x] Implementado

### 11. JSON manual no `AstaController`
- **Arquivo:** `AstaController.java`
- **Problema:** Monta JSON com concatenação de strings, propenso a erros e difícil de manter.
- **Solução:** Usar um DTO/Record e deixar o Jackson serializar automaticamente:
  ```java
  record UploadResponse(boolean success, String message, long timestamp) {}
  return ResponseEntity.ok(new UploadResponse(true, "...", System.currentTimeMillis()));
  ```
- [x] Implementado

### 12. `NullPointerException` potencial no `AstaController`
- **Arquivo:** `AstaController.java`
- **Problema:** `file.getOriginalFilename().endsWith(".asta")` — `getOriginalFilename()` pode retornar `null`.
- **Solução:** Verificação null-safe antes de chamar `.endsWith()`.
- [x] Implementado

### 13. Caminhos hardcoded
- **Arquivos:** `OntologyService.java`, `DiagramsService.java`, `Parser.java`, `OntologiesWriter.java`
- **Problema:** `System.getProperty("user.dir") + "/" + "astah_seon.asta"`, `"./src/main/resources/templates/..."` — Caminhos fixos no código.
- **Solução:** Usar `@Value` com propriedades configuráveis ou `ResourceLoader` do Spring para resolver caminhos.
- [x] Implementado

---

## 🟡 Melhorias de Arquitetura

### 14. CSS duplicado massivamente nos templates
- **Arquivos:** `TemplateHomePage.html`, `TemplateOntologyPage.html`, `UploadPage.html`, `LoginPage.html`
- **Problema:** Cada template tem ~300 linhas de CSS idêntico inline.
- **Solução:** Extrair para um arquivo `static/css/seon-theme.css` e usar `<link>` em todos os templates.
- [x] Implementado

### 15. Thymeleaf Fragments não utilizados
- **Arquivos:** Todos os templates
- **Problema:** Navbar e footer repetidos em cada template.
- **Solução:** Criar `fragments/layout.html` com `th:fragment="navbar"` e `th:fragment="footer"`, e usar `th:replace` nos templates.
- [x] Implementado

### 16. Dependência `javax.servlet` inválida no `build.gradle`
- **Arquivo:** `build.gradle`
- **Problema:** `implementation 'javax.servlet:javax.servlet-api:4.0.1'` — Spring Boot 3.x usa Jakarta EE (`jakarta.servlet`), não `javax.servlet`. Dependência desnecessária e pode causar conflitos.
- **Solução:** Remover essa linha do `build.gradle`.
- [x] Implementado

### 17. `Concept` falta `equals()` e `hashCode()`
- **Arquivo:** `Concept.java`
- **Problema:** Usa `conceptMap` e comparações em listas, mas `Concept` não sobrescreve `equals/hashCode`. O `Comparable` implementado não substitui esses métodos.
- **Solução:** Implementar `equals()` e `hashCode()` baseados no nome e ontologia.
- [x] Implementado

### 18. `OntoLevel` com valores inconsistentes
- **Arquivo:** `Ontology.java`
- **Problema:** `FOUNDATIONAL(1), CORE(1), DOMAIN(2)` — FOUNDATIONAL e CORE têm o mesmo valor `1`.
- **Solução:** Corrigir os valores ou remover o campo `value` se não for utilizado.
- [x] Implementado

---

## 📌 Prioridade de Implementação

| Prioridade | Item | Esforço | Status |
|---|---|---|---|
| 🔴 Alta | #1 - Remover estado estático dos models | Médio | ✅ |
| 🔴 Alta | #2 - Habilitar CSRF | Baixo | ✅ |
| 🔴 Alta | #3 - Senha no .env.example | Trivial | ✅ |
| 🔴 Alta | #16 - Remover `javax.servlet` do build.gradle | Trivial | ✅ |
| 🟠 Média | #5 - Tornar `OntologiesWriter` um `@Component` | Médio | ✅ |
| 🟠 Média | #6 - Thread-safety do `figCount` | Baixo | ✅ |
| 🟠 Média | #8 - Usar Logger em vez de `System.out` | Baixo | ✅ |
| 🟠 Média | #11 - Usar DTOs para respostas JSON | Baixo | ✅ |
| 🟠 Média | #12 - Null-safety no `AstaController` | Trivial | ✅ |
| 🟠 Média | #13 - Caminhos hardcoded | Médio | ✅ |
| 🟡 Baixa | #4 - Substituir `EnvLoader` por lib Spring-dotenv | Baixo | ✅ |
| 🟡 Baixa | #7 - Mover HTML para Thymeleaf | Alto | ✅ |
| 🟡 Baixa | #9 - Remover `PageWriter` legado | Trivial | ✅ |
| 🟡 Baixa | #10 - Refatorar `Parser.java` | Baixo | ✅ |
| 🟡 Baixa | #14 - Extrair CSS para arquivo separado | Médio | ✅ |
| 🟡 Baixa | #15 - Usar Thymeleaf fragments | Médio | ✅ |
| 🟡 Baixa | #17 - Implementar `equals/hashCode` em Concept | Baixo | ✅ |
| 🟡 Baixa | #18 - Corrigir valores de `OntoLevel` | Trivial | ✅ |
