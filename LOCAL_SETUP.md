# Local Development Setup - AlgoMentor Backend

## Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API key (get one at [openai.com/api](https://platform.openai.com/api/keys))
- Docker & Docker Compose (optional, for containerized setup)

## Quick Start (3 steps)

### 1. Configure Environment

Copy the example environment file and add your OpenAI API key:

```bash
cp .env.local.example .env.local
# Edit .env.local and add your OPENAI_API_KEY
```

### 2. Run the Backend

**Option A: Using Maven directly**
```bash
./mvnw spring-boot:run
```

**Option B: Using Docker Compose**
```bash
docker-compose up --build
```

Server will start at: `http://localhost:8080`

### 3. Verify It's Running

Check the health endpoint:
```bash
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "UP",
  "timestamp": "2026-08-22T15:30:00Z"
}
```

## API Endpoints

- **Analyze Solution**: `POST /api/analyze`
- **Chat with Mentor**: `POST /api/chat`
- **API Docs**: `GET /api/swagger-ui.html`
- **OpenAPI Spec**: `GET /api/docs`
- **Health Check**: `GET /api/health`

## Testing

Run unit tests:
```bash
./mvnw test
```

## Troubleshooting

### Port 8080 Already in Use
Change the port in `.env.local`:
```
SERVER_PORT=8081
```

### OPENAI_API_KEY Not Recognized
Make sure `.env.local` is in the root project directory and restart the server.

### CORS Errors When Connecting from Frontend
Update `APP_CORS_ALLOWED_ORIGINS` in `.env.local` to match your frontend URL.

## Development Tips

- Logs are output to console in DEBUG level for `com.algomentor.backend`
- The app uses Spring Boot's hot reload (modify Java files and changes take effect)
- All responses are validated JSON before sending to the client

## Environment Variables Reference

| Variable | Purpose | Default |
|----------|---------|---------|
| `OPENAI_API_KEY` | API key for OpenAI | (required) |
| `SERVER_PORT` | Server port | 8080 |
| `APP_CORS_ALLOWED_ORIGINS` | CORS allowed origins | localhost:5173,5174 |
| `LOGGING_LEVEL_COM_ALGOMENTOR_BACKEND` | Log level for app | DEBUG |
