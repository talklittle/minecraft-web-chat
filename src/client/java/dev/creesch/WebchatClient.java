package dev.creesch;


import dev.creesch.config.ModConfig;
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
		ModConfig.init();
		webInterface = new WebInterface();

		LOGGER.info("web chat loaded");

		// Chat messages from users.
		// TODO: extract more information, put in object serialize to json
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			LOGGER.info("Got chat message: {}", message.getString());
			webInterface.broadcastMessage(message.getString());
		});

		// System messages (joins, leaves, deaths, etc.)
		// TODO: extract more information, put in object serialize to json
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			LOGGER.info("Got game message: {}", message.getString());
			webInterface.broadcastMessage(message.getString());
		});
	}
}