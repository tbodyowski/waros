package de.tbodyowski.waros.Events;


import de.tbodyowski.waros.Main;
import de.tbodyowski.waros.util.Websocket;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.socket.client.Socket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatEvent implements Listener {



    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String playerName = event.getPlayer().getName();
        String message = event.message().toString();

        int insertPosition = message.indexOf("content=\"") + 9;

        String newMessage = message.substring(0, insertPosition) + playerName + ": " + message.substring(insertPosition);

        Main.getInstance().getWebsocket().send(newMessage);
    }
}