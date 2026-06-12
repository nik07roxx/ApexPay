## 🛠️ Local Infrastructure Setup

Until the environment is fully containerized with Docker, use the following paths and commands to spin up the local infrastructure backing Apex Pay.

### Prerequisites Paths
* **Kafka Location:** `C:\ApexPay-Infra\Kafka`
* **Prometheus Location:** `C:\ApexPay-Infra\Prometheus`
* **Grafana Location:** `C:\ApexPay-Infra\Grafana`

### Manual Execution Steps
Each service must be started in its own dedicated command prompt window in the following order:

```cmd
cd C:\ApexPay-Infra\Kafka
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties

cd C:\ApexPay-Infra\Kafka
.\bin\windows\kafka-server-start.bat .\config\server.properties

cd C:\ApexPay-Infra\Prometheus
.\prometheus.exe --config.file=prometheus.yml

cd C:\ApexPay-Infra\Grafana\bin
grafana server
   
