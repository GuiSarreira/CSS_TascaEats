@echo off
REM Script de arranque para desenvolvimento (sem Docker)
REM Ordem correta para evitar NoSuchMethodError no gRPC:
REM  1. Compila UMA vez (protobuf + java)
REM  2. Inicia Spring Boot com as classes ja compiladas (web em 8082, gRPC em 9092)
REM  3. Inicia JavaFX com as mesmas classes (sem recompilar)

echo [1/3] Compilar projeto completo...
call mvnw.cmd clean generate-sources compile -DskipTests
if %errorlevel% neq 0 (
    echo ERRO: Compilacao falhou.
    pause
    exit /b 1
)

echo [2/3] Iniciar servidor Spring Boot (numa nova janela)...
start "TascaEats Server" cmd /k "mvnw.cmd spring-boot:run -Dmaven.main.skip=true -Dprotoc.skip=true -Dmaven.test.skip=true"

echo Aguardar servidor inicializar (20 segundos)...
timeout /t 20 /nobreak > nul

echo [3/3] Iniciar cliente JavaFX...
call mvnw.cmd javafx:run -Dmaven.main.skip=true -Dprotoc.skip=true

pause
