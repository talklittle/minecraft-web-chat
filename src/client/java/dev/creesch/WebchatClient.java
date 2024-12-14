package dev.creesch;


import dev.creesch.config.ModConfig;
import dev.creesch.util.NamedLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebchatClient implements ClientModInitializer {
	private static final NamedLogger LOGGER = new NamedLogger("web-chat");
	//public static final NamedLogger LOGGER = new NamedLogger(LogManager.getFormatterLogger(MOD_NAME));
	private WebInterface webInterface;

	@Override
	public void onInitializeClient() {
		ModConfig.init();
		webInterface = new WebInterface();



		LOGGER.info("web chat loaded");

		// Chat messages from users.
		// TODO: extract more information, put in object serialize to json
		ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world != null) {
				String json = Text.Serialization.toJsonString(message, client.world.getRegistryManager());
				LOGGER.info("Got chat message as JSON: {}", json);
				webInterface.broadcastMessage(json);
			}
		});

		// System messages (joins, leaves, deaths, etc.)
		// TODO: extract more information, put in object serialize to json
		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world != null) {
				String json = Text.Serialization.toJsonString(message, client.world.getRegistryManager());
				LOGGER.info("Got game message as JSON: {}", json);
				webInterface.broadcastMessage(json);
			}
		});


	}
}