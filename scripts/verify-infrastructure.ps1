# Powershell Infrastructure Verification Script for Atomic-Pay

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Verifying Atomic-Pay Infrastructure Setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Check Docker Containers Status
Write-Host "`n[1/4] Checking Docker Containers..." -ForegroundColor Yellow
docker compose ps

# 2. Test PostgreSQL Connection
Write-Host "`n[2/4] Testing PostgreSQL Schemas..." -ForegroundColor Yellow
docker exec -it atomic_pay_postgres psql -U postgres -d atomic_pay -c "\dn"

# 3. Test Redis Connection
Write-Host "`n[3/4] Testing Redis Ping..." -ForegroundColor Yellow
docker exec -it atomic_pay_redis redis-cli ping

# 4. Test Kafka Topics
Write-Host "`n[4/4] Testing Kafka Cluster..." -ForegroundColor Yellow
docker exec -it atomic_pay_kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

Write-Host "`n==========================================" -ForegroundColor Green
Write-Host "Infrastructure Verification Complete!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
