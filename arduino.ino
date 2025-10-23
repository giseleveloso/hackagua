#include <WiFi.h>
#include <ArduinoWebsockets.h>
#include <Adafruit_NeoPixel.h>

using namespace websockets;

// ==== CONFIGURAÇÕES DE REDE ====
const char* ssid = "Caua's iPhone";
const char* password = "44448888";

// ==== CONFIGURAÇÕES DO WEBSOCKET ====
const char* ws_server = "ws://aqualy.tanz.dev/ws/sensor";  // Endereço do seu backend
const char* device_id = "1";                          // ID único do Arduino (ID do medidor no banco)
WebsocketsClient wsClient;

// ==== SENSOR DE FLUXO ====
const int FLOW_PIN = 42;
unsigned long lastTime = 0;
unsigned long lastSendTime = 0;
unsigned long pulseCount = 0;
int lastState = HIGH;
float calibrationFactor = 4.5;  // Pulsos por segundo por L/min

float consumoLitros = 0.0;
float somaVazao = 0.0;
int contagemMedidas = 0;

// ==== LED NEOPIXEL ====
#define LED_PIN 48
#define NUMPIXELS 1
Adafruit_NeoPixel pixel(NUMPIXELS, LED_PIN, NEO_GRB + NEO_KHZ800);

// ==== BOTÃO TOUCH ====
const int TOUCH_PIN = 4;
bool flowSwitchState = true;  // Estado do interruptor de fluxo (ON por padrão)
int lastTouchState = LOW;
unsigned long lastDebounceTime = 0;
unsigned long debounceDelay = 50;

// ==== FUNÇÕES DE LED ====
void setLedColor(uint8_t r, uint8_t g, uint8_t b) {
  pixel.setPixelColor(0, pixel.Color(r, g, b));
  pixel.show();
}

void ledDesconectado() { setLedColor(255, 0, 0); }     // Vermelho
void ledConectado() { setLedColor(0, 255, 0); }        // Verde
void ledEnviando()  { setLedColor(0, 0, 255); }        // Azul (envio)

// ==== SETUP ====
void setup() {
  Serial.begin(115200);
  pinMode(FLOW_PIN, INPUT_PULLUP);
  pinMode(TOUCH_PIN, INPUT);

  pixel.begin();
  ledDesconectado();

  Serial.println("Iniciando conexão Wi-Fi...");
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("\nWi-Fi conectado!");
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

  conectarWebSocket();
}

// ==== FUNÇÃO PARA CONECTAR WEBSOCKET ====
void conectarWebSocket() {
  String url = String(ws_server) + "/" + device_id;
  Serial.print("Conectando ao WebSocket: ");
  Serial.println(url);

  if (wsClient.connect(url.c_str())) {
    Serial.println("✅ Conectado ao WebSocket!");
    ledConectado();
  } else {
    Serial.println("❌ Falha ao conectar ao WebSocket.");
    ledDesconectado();
  }

  // Callback de mensagens recebidas
  wsClient.onMessage([](WebsocketsMessage msg) {
    Serial.print("Mensagem recebida: ");
    Serial.println(msg.data());

    // Parse comando do servidor: 03;ON ou 03;OFF
    String message = msg.data();
    if (message.startsWith("03;")) {
      String command = message.substring(3); // Extrai "ON" ou "OFF"
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
  });

  // Callback de desconexão
  wsClient.onEvent([](WebsocketsEvent event, String data) {
    if (event == WebsocketsEvent::ConnectionClosed) {
      Serial.println("🔴 Conexão WebSocket perdida. Tentando reconectar...");
      ledDesconectado();
    }
  });
}

// ==== LOOP PRINCIPAL ====
void loop() {
  // Mantém o websocket ativo
  if (wsClient.available()) {
    wsClient.poll();
  } else {
    if (WiFi.status() == WL_CONNECTED) {
      conectarWebSocket();
    }
  }

  // Verificar botão touch (com debounce)
  int touchReading = digitalRead(TOUCH_PIN);
  if (touchReading != lastTouchState) {
    lastDebounceTime = millis();
  }
  
  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (touchReading == HIGH && lastTouchState == LOW) {
      // Botão pressionado - toggle do estado
      flowSwitchState = !flowSwitchState;
      Serial.print("👆 Botão touch pressionado - Novo estado: ");
      Serial.println(flowSwitchState ? "ON" : "OFF");
      enviarStatus(flowSwitchState);
      digitalWrite(RELAY_PIN, flowSwitchState ? HIGH : LOW);
    }
  }
  lastTouchState = touchReading;

  // Leitura do sensor de fluxo (só conta pulsos se flow switch estiver ligado)
  if (flowSwitchState) {
    int currentState = digitalRead(FLOW_PIN);
    if (lastState == HIGH && currentState == LOW) {
      pulseCount++;
    }
    lastState = currentState;
  }

  unsigned long currentTime = millis();

  // Cálculo de vazão a cada 1s
  if (currentTime - lastTime >= 1000) {
    float flowRate = pulseCount / calibrationFactor; // L/min
    float litrosPorSegundo = flowRate / 60.0;

    consumoLitros += litrosPorSegundo;
    somaVazao += flowRate;
    contagemMedidas++;

    Serial.print("Vazao Inst.: ");
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
  if (!wsClient.available()) {
    Serial.println("⚠️ WebSocket desconectado — tentativa de reconexão...");
    conectarWebSocket();
    return;
  }

  // Só envia se consumo > 0
  if (consumo <= 0.0) {
    return;
  }

  ledEnviando();
  // Protocolo: 01;{medidorId};{consumoLitros};{vazaoLMin}
  String payload = "01;" + String(device_id) + ";" + String(consumo, 3) + ";" + String(media, 2);
  wsClient.send(payload);
  Serial.println("📤 Dados enviados: " + payload);
  ledConectado();
}

// ==== FUNÇÃO DE ENVIO DE STATUS ====
void enviarStatus(bool estado) {
  if (!wsClient.available()) {
    Serial.println("⚠️ WebSocket desconectado — não foi possível enviar status");
    return;
  }

  ledEnviando();
  // Protocolo: 02;{medidorId};{ON/OFF}
  String status = estado ? "ON" : "OFF";
  String payload = "02;" + String(device_id) + ";" + status;
  wsClient.send(payload);
  Serial.println("📤 Status enviado: " + payload);
  ledConectado();
}
