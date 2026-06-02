@echo off
REM Script de arranque para desenvolvimento (sem Docker para a app)
REM Ordem correta:
REM  0. Inicia PostgreSQL via Docker
REM  1. Compila UMA vez (protobuf + java)
REM  2. Inicia Spring Boot com as classes ja compiladas (web em 8082, gRPC em 9092)
REM  3. Inicia JavaFX com as mesmas classes (sem recompilar)

echo [0/4] Iniciar PostgreSQL, Zookeeper e Kafka via Docker...
docker compose up -d pgserver zookeeper kafka
if %errorlevel% neq 0 (
    echo ERRO: Nao foi possivel iniciar Docker. Certifique-se de que Docker Desktop esta em execucao.
    pause
    exit /b 1
)

echo Aguardar que PostgreSQL e Kafka fiquem prontos (20 segundos)...
timeout /t 20 /nobreak > nul

echo [1/4] Compilar projeto completo...
call mvnw.cmd clean compile -DskipTests
if %errorlevel% neq 0 (
    echo ERRO: Compilacao falhou.
    pause
    exit /b 1
)

echo [2/4] Iniciar servidor Spring Boot (numa nova janela)...
start "TascaEats Server" cmd /k "mvnw.cmd spring-boot:run -Dmaven.main.skip=true -Dprotoc.skip=true -Dmaven.test.skip=true"

echo Aguardar servidor inicializar (30 segundos - Tomcat 8082 + gRPC 9092)...
timeout /t 30 /nobreak > nul

echo [3/4] Iniciar cliente JavaFX...
call mvnw.cmd javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true

echo [4/4] Concluido! Pressione qualquer tecla para limpar.
pause
