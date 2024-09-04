package de.tbodyowski.waros.util;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Websocket {

    private Socket socket;
    private final Logger logger = Logger.getLogger("WarOS");

    // Constructor accepts an existing Socket instance
    public Websocket(Socket socket) {
        this.socket = socket;
        initializeSocket();
    }

    private void initializeSocket() {
        try {
            // Attach event listeners
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    logger.log(Level.INFO, "Connected to WebSocket");
                    try {
                        socket.emit("system", InetAddress.getLocalHost().toString());
                    } catch (UnknownHostException e) {
                        logger.log(Level.WARNING, "Error getting local host address, using loopback", e);
                        socket.emit("system", InetAddress.getLoopbackAddress().toString());
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    logger.log(Level.INFO, "Disconnected from WebSocket");
                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    if (args.length > 0 && args[0] instanceof Throwable) {
                        logger.log(Level.SEVERE, "Error connecting to WebSocket", (Throwable) args[0]);
                    } else {
                        logger.log(Level.SEVERE, "Error connecting to WebSocket", args.length > 0 ? args[0].toString() : "Unknown error");
                    }
                }
            }).on("message", new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    if (objects.length > 0) {
                        Object message = objects[0];
                        if (message instanceof String) {
                            String msg = "§f[§5Discord§f] " + message;
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.sendMessage(msg);
                            }
                        } else {
                            logger.log(Level.WARNING, "Received unexpected message type: {0}", message.getClass().getName());
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize WebSocket", e);
        }
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
        }
    }

    public void send(String message) {
        if (socket != null && socket.connected()) {
            socket.emit("message", message);
        } else {
            logger.log(Level.WARNING, "Cannot send message. WebSocket is not connected.");
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }
}