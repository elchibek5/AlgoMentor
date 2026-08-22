#!/usr/bin/env bash
# Starts the backend, reporting which mode it will run in.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ -f .env.local ] && grep -qE '^OPENAI_API_KEY=(sk-|[A-Za-z0-9_-]{20,})' .env.local; then
  echo "Starting AlgoMentor in LIVE mode (key found in .env.local)."
else
  echo "Starting AlgoMentor in DEMO MODE - analyses are generated offline, not by a model."
  echo "To enable real analysis: cp .env.local.example .env.local, add your OPENAI_API_KEY, rerun."
fi

echo "  API      http://localhost:8080"
echo "  Docs     http://localhost:8080/api/swagger-ui.html"
echo "  Health   http://localhost:8080/api/health"
echo

exec ./mvnw spring-boot:run
