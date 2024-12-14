package dev.creesch;

import dev.creesch.config.ModConfig;
import dev.creesch.util.NamedLogger;
import fi.iki.elonen.NanoHTTPD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class WebInterface {
    private final WebServer webServer;
    private final ChatWebSocketServer websocketServer;
    private final Set<WebSocket> connections = new HashSet<>();

    ModConfig config = ModConfig.HANDLER.instance();

    private static final NamedLogger LOGGER = new NamedLogger("web-chat");

    public WebInterface() {
        webServer = new WebServer(config.httpPortNumber);
        websocketServer = new ChatWebSocketServer(config.httpPortNumber + 1);

        try {
            webServer.start();
            websocketServer.start();
        } catch (IOException e) {
            LOGGER.error("Error with webserver connection", e);
        }
    }

    // HTTP server for static files
    private class WebServer extends NanoHTTPD {
        public WebServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();

            // Serve index.html as default
            if (uri.equals("/")) {
                uri = "/index.html";
            }

            try {
                InputStream inputStream = WebchatClient.class.getResourceAsStream("/web" + uri);
                if (inputStream == null) {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found");
                }

                String mimeType = "text/html";
                if (uri.endsWith(".css")) mimeType = "text/css";
                if (uri.endsWith(".js")) mimeType = "text/javascript";
                if (uri.endsWith(".manifest")) mimeType = "application/manifest+json";

                return newChunkedResponse(Response.Status.OK, mimeType, inputStream);
            } catch (Exception e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error loading file");
            }
        }
    }

    // WebSocket server for chat
    private class ChatWebSocketServer extends WebSocketServer {
        public ChatWebSocketServer(int port) {
            super(new InetSocketAddress(port));
            LOGGER.info("Starting WebSocket server on port " + port);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            LOGGER.info("New connection from " + conn.getRemoteSocketAddress());
            connections.add(conn);
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            LOGGER.info("Closed connection to " + conn.getRemoteSocketAddress() + " with code " + code + " for reason: " + reason);
            connections.remove(conn);
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            LOGGER.info("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
            MinecraftClient client = MinecraftClient.getInstance();
            // Need to schedule on the main thread since we're coming from websocket thread
            client.execute(() -> {
                ClientPlayerEntity player = client.player;
                if (player != null) {
                    player.networkHandler.sendChatMessage(message);
                }
            });
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            LOGGER.error("WebSocket error: ", ex);
            if (conn != null) {
                connections.remove(conn);
            }
        }

        @Override
        public void onStart() {
            LOGGER.info("WebSocket server started successfully");
            setConnectionLostTimeout(30);
        }
    }

    // Method to broadcast messages to all web clients
    public void broadcastMessage(String message) {
        for (WebSocket conn : connections) {
            conn.send(message);
        }
    }
}