@echo off
title Sistema de Pedidos
echo Iniciando Sistema de Pedidos...
echo.
echo Verificando Java...
java -version
echo.
echo Iniciando aplicacao...
java -jar API-2026-1.jar > log.txt 2>&1
if %errorlevel% neq 0 (
    echo.
    echo Erro ao iniciar. Verifique o arquivo log.txt para detalhes.
    pause
)