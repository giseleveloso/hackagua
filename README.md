## 💧 Projeto: Aqualy

### 🧩 Descrição geral

O **Aqualy** é uma aplicação completa para monitoramento de **vazão e consumo de água** em tempo real.
O sistema coleta dados de sensores físicos, envia ao backend desenvolvido em **Quarkus**, e exibe as informações para o usuário final em uma interface desenvolvida em **FlutterFlow**.

---

## 🚀 Tecnologias utilizadas

**Backend:** Quarkus, Java, RESTEasy, Hibernate e PostgreSQL para homologação
**Frontend:** FlutterFlow, Firebase 
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
  "expires_in": 3600
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

* `Medidor(id, nome, localizacao, limite)`
* `Leitura(id, sensor_id, vazao, volume, timestamp)`
* `Usuario(id, nome, email, senha, valorM)`

---

## 📱 Frontend (FlutterFlow)

### 📋 Telas principais

* **Login e cadastro**
* **Home com dados gerais e de cada medidor** (mostra consumo e vazão em tempo real)
* **Dashboard** (detalhamento avançado dos dados monitorados)
* **Histórico de medições**
* **Configuração de sensores**

### 🔌 Integração com API

* Método: REST API (GET/POST)
* Base URL: `<https://aqualy.com>`
* Autenticação: Bearer Token (JWT)

**Exemplo de integração (FlutterFlow Action):**

```dart
POST /leituras
Headers:
  Authorization: Bearer <token>
Body:
  {
    "sensorId": 1,
    "vazao": 3.12,
    "limite": 27.8
  }
```

---

## 🧠 Integração com os sensores físicos

### ⚡ Hardware

* Sensor: `Sensor de vazão - modelo YF-S201`
* Controlador: `ESP32`
* Comunicação: `<HTTP / MQTT / Serial>`
* Frequência de envio: `<ex: a cada 5 segundos>`

**Exemplo de payload enviado ao backend:**

```json
{
  "sensorId": 1,
  "flowRate": 1.86,
  "volume": 12.5
}
```

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
DB_URL=jdbc:postgresql://localhost:5432/water
DB_USER=admin
DB_PASS=1234
JWT_SECRET=seu_token_aqui
```

---

### 📱 Frontend (FlutterFlow)

1. Abrir o projeto no FlutterFlow.
2. Atualizar a URL da API em **App Settings > API Configuration**.
3. Publicar o app (web ou mobile).

---

## 🧪 Testes

Para testar o envio de dados manualmente:

```bash
curl -X POST https://api.seuprojeto.com/measurements \
  -H "Content-Type: application/json" \
  -d '{"sensorId":1,"flowRate":2.8,"volume":14.2}'
```

---

## 🧭 Roadmap

* [x] Criar o backend funcional
* [x] Integrar o frontend com o backend
* [ ] Desenvolver a parte dos sensores isoladamente
* [ ] Integrar os sensores com o backend
* [ ] Implementar alertas de vazamento
* [ ] Criar dashboard em tempo real

---

## 👥 Autores

* **Cauã Fernandes, Dejanildo Júnior, Gisele Veloso, João Víttor Costa e Thalyssa Freitas**

---

# UNITINS - HACKÁGUA

