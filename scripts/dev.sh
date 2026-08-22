#!/bin/bash
# Development startup script for AlgoMentor Backend

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting AlgoMentor Backend...${NC}"

# Check if .env.local exists
if [ ! -f ".env.local" ]; then
  echo -e "${YELLOW}⚠️  No .env.local found${NC}"
  echo -e "${BLUE}Creating .env.local from template...${NC}"
  cp .env.local.example .env.local
  echo -e "${GREEN}✓ Created .env.local${NC}"
  echo -e "${YELLOW}⚠️  Please edit .env.local and add your OPENAI_API_KEY${NC}"
  exit 1
fi

# Check if OPENAI_API_KEY is set
if ! grep -q "OPENAI_API_KEY=" .env.local || grep -q "OPENAI_API_KEY=your-openai-api-key-here" .env.local; then
  echo -e "${YELLOW}⚠️  OPENAI_API_KEY not configured in .env.local${NC}"
  echo -e "${BLUE}Please edit .env.local and add your OpenAI API key${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Environment configured${NC}"
echo -e "${BLUE}Starting backend server...${NC}"
echo -e "${BLUE}Server will be available at http://localhost:8080${NC}"
echo -e "${BLUE}API Docs: http://localhost:8080/api/swagger-ui.html${NC}"

./mvnw spring-boot:run
