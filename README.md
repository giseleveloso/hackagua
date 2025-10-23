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

* Tipo: JWT 
* Endpoint de login: `POST /login`
* Exemplo de resposta:

```json
{
  "token": "<jwt_token>",
  "Usuario": <Usuario>
}
```

---

### 🌊 Endpoints principais

#### 🔹 **Sensores**

`GET /medidores`
Retorna a lista de sensores cadastrados.

**Exemplo de resposta:**

```json
[
  {
    "id": 1,
    "name": "Sensor 1",
    "location": "Pia da Cozinha",
    "limite": 1000
  }
]
```

`POST /medidores`
Cadastra um novo sensor.

```json
{
  "name": "Sensor 2",
  "location": "Maquina de lavar"
}
```

---

#### 🔹 **Medições**

`POST /leituras`
Recebe dados de vazão enviados pelo sensor.

```json
{
  "sensorId": 1,
  "vazao": 2.45,
  "consumoTotal": 13.7
}
```

`GET /leituras/{medidorId}`
Retorna histórico de medições de um sensor.

---

#### 🔹 **Usuários**

`POST /usuarios`
Cria um novo usuário.
`GET /usuarios/{id}`
Retorna dados do usuário.

---

### 🗄️ Banco de dados

Entidades principais:

* `Medidor`
* `Leitura`
* `Usuario`

---

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

