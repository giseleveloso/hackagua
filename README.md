## 💧 Projeto: Aqualy

### 🧩 Descrição geral

O **Aqualy** é uma aplicação completa para monitoramento de **vazão e consumo de água** em tempo real.
O sistema coleta dados de sensores físicos, envia ao backend desenvolvido em **Quarkus**, e exibe as informações para o usuário final em uma interface desenvolvida em **FlutterFlow**.

---

## 🚀 Tecnologias utilizadas

**Backend:** Quarkus, Java, RESTEasy, Hibernate, PostgreSQL (para homologação)
**Frontend:** FlutterFlow 
**Banco de dados:** MariaDB
**Integração:** HTTP para integrar com o sensor

---

## ⚙️ Backend (API Quarkus)

### 📁 Estrutura geral

```
src/
 ├── main/
 │   ├── java/br/unitins/topicos1/
 │   │    ├── dto/
 │   │    ├── form/
 │   │    ├── model/
 │   │    ├── repository/
 │   │    ├── resource/
 │   │    ├── service/
 │   │    ├── util/
 │   │    └── validation/
 │   └── resources/
 │        └── application.properties
```

### 🔐 Autenticação

- Tipo: JWT
- Endpoints:
  - `POST /auth/login`
  - `POST /auth/registro`
- Exemplo de resposta (login):

```json
{
  "token": "<jwt_token>",
  "usuario": { /* UsuarioResponseDTO */ }
}
```

---

### 🧱 Arquitetura (camadas)

- `resource/` (REST & WS): recebe requisições HTTP/WebSocket e delega para serviços.
  - REST: `AuthResource`, `UsuarioResource`, `MedidorResource`, `LeituraResource`, `SugestaoResource`
  - WebSocket: `resource/ws/SensorSocket` (canal com os medidores)
- `service/`: regras de negócio (ex.: `LeituraServiceImpl`, `EstatisticaServiceImpl`, `SugestaoServiceImpl`)
- `repository/`: acesso a dados com Panache (ex.: `LeituraRepository`, `MedidorRepository`, `UsuarioRepository`)
- `model/`: entidades JPA (ex.: `Usuario`, `Medidor`, `Leitura`)
- `dto/`: contratos de entrada/saída (ex.: `MedidorDTO`, `MedidorResponseDTO`, `TempoRealResponseDTO`)
- `util/` e `validation/`: utilitários e validações

---

### 🌊 Endpoints principais

#### 🔹 Autenticação
- `POST /auth/login` — autentica e retorna JWT
- `POST /auth/registro` — cria usuário e retorna `UsuarioResponseDTO`

#### 🔹 Usuários
- `GET /usuarios` — lista usuários
- `GET /usuarios/{id}` — detalhe do usuário
- `PUT /usuarios/{id}` — atualiza e retorna `UsuarioResponseDTO`

#### 🔹 Medidores
- `GET /medidores` — lista medidores
- `GET /medidores/{id}` — detalhe do medidor
- `GET /medidores/usuario/{usuarioId}` — por usuário
- `PUT /medidores/{id}/power?ligado=true|false` — atualiza estado e retorna `MedidorResponseDTO`
- `PUT /medidores/{id}/power/toggle` — alterna estado e retorna `MedidorResponseDTO`

#### 🔹 Leituras, Estatísticas e Tempo real
- `GET /leituras/estatisticas/medidor/{medidorId}?dataInicio&dataFim`
- `GET /leituras/estatisticas/medidor/{medidorId}/hoje`
- `GET /leituras/estatisticas/medidor/{medidorId}/semana`
- `GET /leituras/estatisticas/medidor/{medidorId}/mes`
- `GET /leituras/tempo-real/medidor/{medidorId}` — `TempoRealResponseDTO`
- `GET /leituras/tempo-real/usuario/{usuarioId}` — lista de `TempoRealResponseDTO`

Observação: as leituras em tempo real são recebidas via WebSocket (ver abaixo) e persistidas pelo backend.

#### 🔹 Sugestões com IA
- `GET /sugestoes/medidor/{medidorId}?dataInicio&dataFim` — retorna `SugestaoIaResponseDTO`

#### 🔹 WebSocket (sensores)
- Endpoint: `ws://<host>:<port>/ws/sensor/{uuid}`
- Mensagens de entrada (sensor → servidor):
  - `01;{medidorId};{consumoLitros};{vazaoLMin}` — registra leitura
- Comandos de saída (servidor → sensor):
  - `03;ON` ou `03;OFF` — liga/desliga

---

### 🗄️ Arquitetura de Banco

Entidades e relacionamentos (simplificado):

- `Usuario (id, nome, email[único], senha, valorM)`
  - 1:N `Usuario` → `Medidor`
- `Medidor (id, nome, localizacao, limite, ligado, interromper, usuario_id)`
  - N:1 `Medidor` → `Usuario`
  - 1:N `Medidor` → `Leitura`
- `Leitura (id, medidor_id, litros, litros_acumulado, vazao_l_min, data_hora)`
  - N:1 `Leitura` → `Medidor`

---

## 🧠 Integração com os sensores físicos

### ⚡ Hardware

* Sensor: `Sensor de vazão - modelo YF-S201`
* Controlador: `ESP32`
* Comunicação: `<HTTP / MQTT / Serial / WebSocket>`

---

## 🧩 Como executar o projeto

### 🖥️ Backend

```bash
# Clonar o repositório
git clone https://github.com/giseleveloso/hackagua.git

# Executar em modo dev
./mvnw quarkus:dev
```

**Variáveis de ambiente:**

```
DEV_DB_TYPE=postgresql
DEV_DB_USER=topicos1
DEV_DB_PASSWORD=123456
DEV_DB_ADDRESS=jdbc:postgresql://localhost:5432/hackagua
DEV_DB_NAME=aqualy

GEMINI_API_KEY=AIzaSyAeChHj8i7ifk08eRlcF-j2TZDDJSkgMhM

QUARKUS_HTTP_PORT=10017
```

---

## 🧪 Testes

Para testar o envio de dados utilizamos o Swagger UI

---

## 👥 Autores

* **Cauã Fernandes, Dejanildo Júnior, Gisele Veloso, João Víttor Costa e Thalyssa Freitas**

---

# UNITINS - HACKÁGUA

