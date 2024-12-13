package dev.creesch;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebchatClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("web-chat");
	private WebInterface webInterface;

	@Override
	public void onInitializeClient() {
		LOGGER.info("web chat loaded");
		webInterface = new WebInterface();

		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			// Broadcast chat messages to web clients
			LOGGER.info("Got client message: {}", message.getString());
			webInterface.broadcastMessage(message.getString());
		});
	}


	private void sendChatMessage(String message) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player != null) {
			player.networkHandler.sendChatMessage(message);
		}
	}
}