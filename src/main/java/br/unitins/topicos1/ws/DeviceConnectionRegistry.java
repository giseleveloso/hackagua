package br.unitins.topicos1.ws;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeviceConnectionRegistry {

	private final ConcurrentMap<Long, WebSocketConnection> connectionByDeviceId = new ConcurrentHashMap<>();
	private final ConcurrentMap<WebSocketConnection, Long> deviceIdByConnection = new ConcurrentHashMap<>();

	public void register(long deviceId, WebSocketConnection connection) {
		WebSocketConnection previous = connectionByDeviceId.put(deviceId, connection);
		if (previous != null && previous.isOpen()) {
			previous.close((short) 1000, "Replaced by new connection");
		}
		deviceIdByConnection.put(connection, deviceId);
	}

	public Optional<Long> getDeviceId(WebSocketConnection connection) {
		return Optional.ofNullable(deviceIdByConnection.get(connection));
	}

	public Optional<WebSocketConnection> getConnection(long deviceId) {
		return Optional.ofNullable(connectionByDeviceId.get(deviceId));
	}

	public void remove(WebSocketConnection connection) {
		Long deviceId = deviceIdByConnection.remove(connection);
		if (deviceId != null) {
			connectionByDeviceId.remove(deviceId, connection);
		}
	}

	public Set<Long> activeDeviceIds() {
		return Set.copyOf(connectionByDeviceId.keySet());
	}
}


