# Odontologia SaaS

Aplicacao com backend Spring Boot, frontend Angular e banco PostgreSQL.

## Rodar localmente

### Banco

Crie um banco PostgreSQL local:

```sql
CREATE DATABASE odonto;
```

Por padrao o backend usa:

```text
url: jdbc:postgresql://localhost:5432/odonto
usuario: postgres
senha: admin
```

Se quiser mudar sem editar codigo, defina as variaveis `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`.

### Backend

```bash
cd backend
mvn spring-boot:run
```

API local:

```text
http://localhost:8080/api
```

### Frontend

```bash
cd frontend
npm install
npm start
```

Frontend local:

```text
http://localhost:4200
```

O frontend local usa `frontend/src/environments/environment.ts`, que aponta para `http://localhost:8080/api`.

## Rodar em producao

### Opcao recomendada: Docker Compose

No servidor, instale Docker e Docker Compose:

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
```

Copie o exemplo de ambiente e ajuste os valores:

```bash
cp .env.example .env
nano .env
```

Valores obrigatorios para trocar:

```env
POSTGRES_PASSWORD=senha-forte
APP_CORS_ALLOWED_ORIGINS=https://app.seudominio.com.br
APP_JWT_SECRET=uma-chave-grande-com-40-ou-mais-caracteres
GOOGLE_BUSINESS_PROFILE_REDIRECT_URI=https://app.seudominio.com.br/api/integracoes/google-business-profile/callback
```

Suba o ambiente:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Ver logs:

```bash
docker compose -f docker-compose.prod.yml logs -f backend
```

Parar:

```bash
docker compose -f docker-compose.prod.yml down
```

O frontend fica publicado na porta configurada em `HTTP_PORT`, por padrao `80`.

Se estiver subindo um banco totalmente novo, crie as tabelas antes de usar o sistema. O projeto mantem `SPRING_JPA_HIBERNATE_DDL_AUTO=none` por seguranca. Para um primeiro ambiente de teste, voce pode usar temporariamente `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, subir o backend, validar as tabelas e depois voltar para `none`.

### Opcao manual: Java e Nginx

### Backend

No servidor, configure as variaveis de ambiente. Use `backend/.env.example` como referencia.

Variaveis essenciais:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/odonto
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=senha-forte
APP_JWT_SECRET=uma-chave-grande-com-40-ou-mais-caracteres
APP_CORS_ALLOWED_ORIGINS=https://app.seudominio.com.br
SERVER_PORT=8080
```

Build do backend:

```bash
cd backend
mvn clean package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend

Build de producao:

```bash
cd frontend
npm install
npm run build:prod
```

O build de producao usa `frontend/src/environments/environment.prod.ts`, com API em `/api`.

Isso foi pensado para Nginx servir o Angular e encaminhar `/api` para o backend:

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

location / {
    try_files $uri $uri/ /index.html;
}
```

## WhatsApp

O WhatsApp manual abre pelo navegador do usuario e deve funcionar em local e producao.

O envio automatico pelo backend so funciona com um provedor/API configurado:

```env
WHATSAPP_ENABLED=true
WHATSAPP_API_URL=https://url-do-provedor
WHATSAPP_TOKEN=token-do-provedor
```

O backend envia para o provedor um JSON no formato:

```json
{"phone":"5511999999999","message":"mensagem"}
```

Se o provedor escolhido usar outro formato, ajuste `WhatsappService`.
