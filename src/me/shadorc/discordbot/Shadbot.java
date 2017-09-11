package me.shadorc.discordbot;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import me.shadorc.discordbot.data.Config;
import me.shadorc.discordbot.data.Config.APIKey;
import me.shadorc.discordbot.data.Stats;
import me.shadorc.discordbot.data.Storage;
import me.shadorc.discordbot.events.ReadyListener;
import me.shadorc.discordbot.events.ShardListener;
import me.shadorc.discordbot.music.GuildMusicManager;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Shadbot {

	private static IDiscordClient client;

	public static void main(String[] args) {
		Storage.init();
		Config.load();
		Stats.init();

		client = new ClientBuilder()
				.withToken(Config.get(APIKey.DISCORD_TOKEN))
				.setMaxMessageCacheCount(250)
				.setMaxReconnectAttempts(100)
				.login();

		client.getDispatcher().registerListener(new ReadyListener());
		client.getDispatcher().registerListener(new ShardListener());

		AudioSourceManagers.registerRemoteSources(GuildMusicManager.PLAYER_MANAGER);
	}

	public static IDiscordClient getClient() {
		return client;
	}
}