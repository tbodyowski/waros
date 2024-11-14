package de.tbodyowski.waros.util;

import de.tbodyowski.waros.Main;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
            }).on("whitelist", new Emitter.Listener() {
                @Override
                public void call(Object... objects) {
                    if (objects.length > 0) {
                        Object message = objects[0];
                        try {
                            logger.log(Level.INFO, "Received message: {0}", message);

                            // Get OfflinePlayer (can represent both online and offline players)
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(String.valueOf(message));

                            if (offlinePlayer != null) {
                                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                                    offlinePlayer.setWhitelisted(true);
                                    logger.log(Level.INFO, "Whitelisted player: {0}", offlinePlayer.getName());
                                });
                            } else {
                                logger.log(Level.WARNING, "Player not found or not valid: {0}", message);
                            }
                        } catch (Exception e) {

                        }
                    } else {
                        logger.log(Level.WARNING, "Received unexpected message type.");
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
    public void link(String message) {
        if (socket != null && socket.connected()) {
            socket.emit("link", message);
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