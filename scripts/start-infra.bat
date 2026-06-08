```cmd
@echo off
title Apex Pay Infrastructure Orchestrator
echo ===================================================
echo   LAUNCHING APEXPAY LOCAL INFRASTRUCTURE PLATFORM
echo ===================================================
echo.

echo [1/4] Launching ZooKeeper Storage...
start "Infrastructure: ZooKeeper" cmd /k "cd /d C:\ApexPay-Infra\Kafka && .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties"

echo Waiting for ZooKeeper to stabilize (5 seconds)...
timeout /t 5 /nobreak > nul

echo [2/4] Launching Kafka Event Broker...
start "Infrastructure: Kafka Broker" cmd /k "cd /d C:\ApexPay-Infra\Kafka && .\bin\windows\kafka-server-start.bat .\config\server.properties"

echo [3/4] Launching Prometheus Time-Series Database...
start "Infrastructure: Prometheus Metrics" cmd /k "cd /d C:\ApexPay-Infra\Prometheus && .\prometheus.exe --config.file=prometheus.yml"

echo [4/4] Launching Grafana Analytics UI Server...
start "Infrastructure: Grafana Dashboard" cmd /k "cd /d C:\ApexPay-Infra\Grafana\bin && .\grafana-server.exe"

echo.
echo ===================================================
echo  ALL WINDOWS INITIALIZED SUCCESSFULLY!
echo  Keep the running terminals open during debugging.
echo ===================================================
pause