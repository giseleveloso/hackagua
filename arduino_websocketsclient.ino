#include <WiFi.h>
#include <WebSocketsClient.h>
#include <Adafruit_NeoPixel.h>

// ==== CONFIGURAÇÕES DE REDE ====
const char* ssid = "UNITINS_WIFI";
const char* password = "";

// ==== CONFIGURAÇÕES DO WEBSOCKET ====
const char* ws_host = "aqualy.tanz.dev";
const uint16_t ws_port = 443;  // Porta HTTPS
const char* ws_path = "/ws/sensor/1";  // Caminho com ID do medidor
const char* device_id = "1";

WebSocketsClient webSocket;

// ==== SENSOR DE FLUXO ====
const int FLOW_PIN = 15;
unsigned long lastTime = 0;
unsigned long lastSendTime = 0;
unsigned long pulseCount = 0;
int lastState = HIGH;
float calibrationFactor = 4.5;

float consumoLitros = 0.0;
float somaVazao = 0.0;
int contagemMedidas = 0;

// ==== RELÉ (INTERRUPTOR DE FLUXO) ====
const int RELAY_PIN = 8;

// ==== LED NEOPIXEL ====
#define LED_PIN 48
#define NUMPIXELS 1
Adafruit_NeoPixel pixel(NUMPIXELS, LED_PIN, NEO_GRB + NEO_KHZ800);

// ==== BOTÃO TOUCH ====
const int TOUCH_PIN = 4;
bool flowSwitchState = true;
int lastTouchState = LOW;
unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;

// ==== CONTROLE DE CONEXÃO ====
bool wsConnected = false;

// ==== FUNÇÕES DE LED ====
void setLedColor(uint8_t r, uint8_t g, uint8_t b) {
  pixel.setPixelColor(0, pixel.Color(r, g, b));
  pixel.show();
}

void ledDesconectado() { setLedColor(255, 0, 0); }     // Vermelho
void ledConectado() { setLedColor(0, 255, 0); }        // Verde
void ledEnviando()  { setLedColor(0, 0, 255); }        // Azul

// ==== CALLBACK DO WEBSOCKET ====
void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
  switch(type) {
    case WStype_DISCONNECTED:
      Serial.println("🔴 WebSocket Desconectado!");
      wsConnected = false;
      ledDesconectado();
      break;
      
    case WStype_CONNECTED:
      Serial.println("✅ WebSocket Conectado!");
      Serial.printf("📍 Conectado em: %s\n", payload);
      wsConnected = true;
      ledConectado();
      break;
      
    case WStype_TEXT: {
      Serial.printf("📨 Mensagem recebida: %s\n", payload);
      
      // Parse comando do servidor: 03;ON ou 03;OFF
      String message = String((char*)payload);
      if (message.startsWith("03;")) {
        String command = message.substring(3);
        if (command == "ON") {
          Serial.println("🟢 Fluxo LIGADO (comando remoto)");
          flowSwitchState = true;
          digitalWrite(RELAY_PIN, HIGH);
        } else if (command == "OFF") {
          Serial.println("🔴 Fluxo DESLIGADO (comando remoto)");
          flowSwitchState = false;
          digitalWrite(RELAY_PIN, LOW);
        }
      }
      break;
    }
      
    case WStype_ERROR:
      Serial.printf("❌ Erro WebSocket: %s\n", payload);
      ledDesconectado();
      break;
      
    case WStype_BIN:
      Serial.printf("⚠️ Dados binários recebidos (%u bytes)\n", length);
      break;
  }
}

// ==== SETUP ====
void setup() {
  Serial.begin(115200);
  delay(100);
  
  Serial.println("\n\n========================================");
  Serial.println("🚀 Iniciando Sistema de Medição");
  Serial.println("========================================");
  
  pinMode(FLOW_PIN, INPUT_PULLUP);
  pinMode(TOUCH_PIN, INPUT);
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, HIGH);

  pixel.begin();
  ledDesconectado();

  // Inicializa estado do pino do sensor para detecção de borda consistente
  lastState = digitalRead(FLOW_PIN);

  // Conectar WiFi
  Serial.println("\n📡 Conectando ao WiFi...");
  Serial.printf("SSID: %s\n", ssid);
  WiFi.begin(ssid, password);

  int tentativas = 0;
  while (WiFi.status() != WL_CONNECTED && tentativas < 30) {
    delay(500);
    Serial.print(".");
    tentativas++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✅ WiFi Conectado!");
    Serial.print("📍 IP Local: ");
    Serial.println(WiFi.localIP());
    Serial.print("📶 RSSI: ");
    Serial.print(WiFi.RSSI());
    Serial.println(" dBm");
  } else {
    Serial.println("\n❌ Falha ao conectar WiFi!");
    return;
  }

  // Configurar WebSocket
  Serial.println("\n🔌 Configurando WebSocket...");
  Serial.printf("Host: %s\n", ws_host);
  Serial.printf("Porta: %u\n", ws_port);
  Serial.printf("Path: %s\n", ws_path);
  
  // Configurar SSL/TLS
  webSocket.beginSSL(ws_host, ws_port, ws_path);
  
  // Configurar evento
  webSocket.onEvent(webSocketEvent);
  
  // Configurar reconexão automática
  webSocket.setReconnectInterval(5000);
  
  // Configurar headers customizados
  webSocket.setExtraHeaders("Origin: https://aqualy.tanz.dev");
  
  Serial.println("✅ WebSocket configurado!");
  Serial.println("========================================\n");
}

// ==== LOOP PRINCIPAL ====
void loop() {
  // Processar eventos WebSocket
  webSocket.loop();

  // Verificar WiFi
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("🔴 WiFi desconectado! Tentando reconectar...");
    ledDesconectado();
    WiFi.begin(ssid, password);
    delay(5000);
    return;
  }

  // Verificar botão touch (com debounce)
  int touchReading = digitalRead(TOUCH_PIN);
  if (touchReading != lastTouchState) {
    lastDebounceTime = millis();
  }
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (touchReading == HIGH && lastTouchState == LOW) {
      flowSwitchState = !flowSwitchState;
      Serial.print("👆 Botão touch - Novo estado: ");
      Serial.println(flowSwitchState ? "ON" : "OFF");
      enviarStatus(flowSwitchState);
      digitalWrite(RELAY_PIN, flowSwitchState ? HIGH : LOW);
    }
  }
  lastTouchState = touchReading;

  // Leitura do sensor de fluxo (sempre conta, sem depender do estado do relé)
  int currentState = digitalRead(FLOW_PIN);
  if (lastState == HIGH && currentState == LOW) {
    pulseCount++;
  }
  lastState = currentState;

  unsigned long currentTime = millis();

  // Cálculo de vazão a cada 1s
  if (currentTime - lastTime >= 1000) {
    float flowRate = pulseCount / calibrationFactor;
    float litrosPorSegundo = flowRate / 60.0;

    consumoLitros += litrosPorSegundo;
    somaVazao += flowRate;
    contagemMedidas++;

    Serial.print("💧 Vazão: ");
    Serial.print(flowRate, 2);
    Serial.println(" L/min");

    pulseCount = 0;
    lastTime = currentTime;
  }

  // Envio de dados a cada 10s
  if (currentTime - lastSendTime >= 10000) {
    float mediaVazao = (contagemMedidas > 0) ? somaVazao / contagemMedidas : 0.0;

    enviarDados(consumoLitros, mediaVazao);

    consumoLitros = 0.0;
    somaVazao = 0.0;
    contagemMedidas = 0;
    lastSendTime = currentTime;
  }
}

// ==== FUNÇÃO DE ENVIO DE DADOS ====
void enviarDados(float consumo, float media) {
  if (!wsConnected) {
    Serial.println("⚠️ WebSocket desconectado - não enviando dados");
    return;
  }

  if (consumo <= 0.0) {
    return;
  }

  ledEnviando();
  
  // Protocolo: 01;{medidorId};{consumoLitros};{vazaoLMin}
  String payload = "01;" + String(device_id) + ";" + String(consumo, 3) + ";" + String(media, 2);
  webSocket.sendTXT(payload);
  
  Serial.println("📤 Dados enviados: " + payload);
  
  delay(100);
  ledConectado();
}

// ==== FUNÇÃO DE ENVIO DE STATUS ====
void enviarStatus(bool estado) {
  if (!wsConnected) {
    Serial.println("⚠️ WebSocket desconectado - não enviando status");
    return;
  }

  ledEnviando();
  
  // Protocolo: 02;{medidorId};{ON/OFF}
  String status = estado ? "ON" : "OFF";
  String payload = "02;" + String(device_id) + ";" + status;
  webSocket.sendTXT(payload);
  
  Serial.println("📤 Status enviado: " + payload);
  
  delay(100);
  ledConectado();
}

