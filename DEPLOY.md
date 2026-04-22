# Deploy do SEON no Servidor NEMO

Guia para publicar a aplicação SEON Spring Boot no servidor `dev.nemo.inf.ufes.br`.

---

## 1. Conectar ao Servidor via SSH

```bash
ssh seu_usuario@dev.nemo.inf.ufes.br
```

> Substitua `seu_usuario` pelo nome de usuário que você recebeu.

---

## 2. Verificar se o Java 21 está instalado

```bash
java -version
```

Se não estiver instalado ou for uma versão anterior:

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y
```

---

## 3. Criar o diretório do sistema

```bash
sudo mkdir -p /var/www/html/seon
sudo chown seu_usuario:seu_usuario /var/www/html/seon
```

---

## 4. Enviar os arquivos via FileZilla (SFTP)

1. Abra o **FileZilla**
2. Configure a conexão:
   - **Protocolo**: SFTP – SSH File Transfer Protocol
   - **Host**: `dev.nemo.inf.ufes.br`
   - **Porta**: 22
   - **Tipo de login**: Normal
   - **Usuário/Senha**: os dados que você recebeu
3. Transfira **todo o repositório** para `/var/www/html/seon/`

**Arquivos obrigatórios:**

| Arquivo/Pasta | Descrição |
|---|---|
| `gradlew` | Script do Gradle Wrapper |
| `build.gradle` | Configuração de dependências |
| `settings.gradle` | Configuração do projeto Gradle |
| `src/` | Código-fonte e recursos |
| `jars/` | JARs proprietários do Astah API |
| `gradle/` | Wrapper do Gradle |
| `astah_seon.asta` | Arquivo Astah principal |
| `.env.example` | Template de variáveis de ambiente |

---

## 5. Configurar permissões e variáveis de ambiente

No servidor, via SSH:

```bash
cd /var/www/html/seon

# Tornar o gradlew executável
chmod +x gradlew

# Tornar o script do Astah executável
chmod +x jars/astah-command.sh

# Criar o arquivo .env com credenciais seguras
cp .env.example .env
nano .env
```

Edite o `.env`:

```properties
SEON_ADMIN_USERNAME=admin
SEON_ADMIN_PASSWORD=uma_senha_segura_aqui
```

---

## 6. Configurar a porta da aplicação

O servidor já usa as seguintes portas:

| Porta | Serviço |
|---|---|
| 80 | Nginx |
| 8080 | Tomcat |
| 8082 | WildFly |
| 5432 | PostgreSQL |

Use uma **porta diferente**, por exemplo **8090**. Adicione ao `application.properties`:

```bash
echo "server.port=8090" >> src/main/resources/application.properties
```

---

## 7. Fazer o build do projeto

```bash
cd /var/www/html/seon
./gradlew bootJar
```

Isso gera o JAR executável em `build/libs/seon-1.0-SNAPSHOT.jar`.

---

## 8. Testar a execução

```bash
cd /var/www/html/seon
java -Xss4m -jar build/libs/seon-1.0-SNAPSHOT.jar
```

Verifique se funciona acessando internamente: `http://localhost:8090`

Pare com `Ctrl+C` após confirmar.

---

## 9. Criar um serviço systemd

Isso permite que a aplicação rode automaticamente e reinicie em caso de falha.

```bash
sudo nano /etc/systemd/system/seon.service
```

Cole o conteúdo abaixo (substituindo `seu_usuario`):

```ini
[Unit]
Description=SEON Spring Boot Application
After=network.target

[Service]
User=seu_usuario
WorkingDirectory=/var/www/html/seon
ExecStart=/usr/bin/java -jar /var/www/html/seon/build/libs/seon-1.0-SNAPSHOT.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Ative o serviço:

```bash
sudo systemctl daemon-reload
sudo systemctl enable seon
sudo systemctl start seon
```

Verificar status:

```bash
sudo systemctl status seon
```

---

## 10. Configurar o Nginx como proxy reverso

```bash
sudo nano /etc/nginx/sites-enabled/myapp.conf
```

Adicione **dentro do bloco `server {}`** existente (sem alterar o que já existe):

```nginx
location /seon/ {
    proxy_pass http://localhost:8090/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

Teste e reinicie o Nginx:

```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

## 11. Acessar o sistema

O sistema estará disponível em:

> **https://dev.nemo.inf.ufes.br/seon/**

---

## Pontos de Atenção

### Context Path

Como o Nginx faz proxy de `/seon/` para `/` (raiz da app), pode ser necessário configurar o context path no Spring Boot para que links e recursos estáticos funcionem corretamente.

Adicione ao `application.properties`:

```properties
server.servlet.context-path=/seon
```

Se fizer isso, altere o proxy no Nginx para:

```nginx
location /seon/ {
    proxy_pass http://localhost:8090/seon/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

### Astah API

Os JARs do Astah precisam estar presentes na pasta `jars/` no servidor. Verifique se o script `jars/astah-command.sh` funciona corretamente no ambiente Linux do servidor.

### Imagens geradas

Na inicialização, o SEON exporta diagramas para `src/main/resources/static/images/`. Certifique-se que o usuário do serviço tem permissão de escrita nesse diretório.

### Logs

Para acompanhar os logs da aplicação em tempo real:

```bash
sudo journalctl -u seon -f
```

---

## Comandos úteis

| Ação | Comando |
|---|---|
| Iniciar o SEON | `sudo systemctl start seon` |
| Parar o SEON | `sudo systemctl stop seon` |
| Reiniciar o SEON | `sudo systemctl restart seon` |
| Ver status | `sudo systemctl status seon` |
| Ver logs | `sudo journalctl -u seon -f` |
| Rebuild após atualização | `cd /var/www/html/seon && ./gradlew bootJar` |
| Testar config do Nginx | `sudo nginx -t` |
| Reiniciar Nginx | `sudo systemctl restart nginx` |

---

## Atualizações futuras

Para atualizar o sistema, envie os novos arquivos via FileZilla, refaça o build e reinicie:

```bash
cd /var/www/html/seon
./gradlew bootJar
sudo systemctl restart seon
```
