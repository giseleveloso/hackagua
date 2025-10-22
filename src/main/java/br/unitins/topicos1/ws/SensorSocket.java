package br.unitins.topicos1.ws;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

@WebSocket(path = "/ws/sensor")
public class SensorSocket {

	@Inject
	DeviceConnectionRegistry registry;

	@OnOpen
	void onOpen(WebSocketConnection connection) {
        
		// Aguarda primeira mensagem com o deviceId
	}

	@OnTextMessage
	void onText(String message, WebSocketConnection connection) {
		var maybeId = registry.getDeviceId(connection);
		if (maybeId.isEmpty()) {
			String deviceId = extractId(message);
			registry.register(deviceId, connection);
			connection.sendTextAndAwait("registered:" + deviceId);
			return;
		}

		String deviceId = maybeId.get();
		handleSensorData(deviceId, message);
	}

	@OnClose
	void onClose(WebSocketConnection connection) {
		registry.remove(connection);
	}

	@OnError
	void onError(WebSocketConnection connection, Throwable error) {
		registry.remove(connection);
	}

	private static String extractId(String msg) {
		String trimmed = msg.trim();
		if (trimmed.startsWith("{") && trimmed.contains("\"id\"")) {
			int i = trimmed.indexOf("\"id\"");
			int c = trimmed.indexOf(':', i);
			int q1 = trimmed.indexOf('\"', c);
			int q2 = trimmed.indexOf('\"', q1 + 1);
			if (q1 > 0 && q2 > q1) {
				return trimmed.substring(q1 + 1, q2);
			}
		}
		return trimmed;
	}

	private void handleSensorData(String deviceId, String message) {
		// Integre com serviços existentes; por enquanto apenas log/placeholder
	}
}


