package me.shadorc.shadbot.music;

import java.util.concurrent.ConcurrentHashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import discord4j.core.DiscordClient;
import discord4j.core.object.util.Snowflake;
import me.shadorc.shadbot.utils.executor.ScheduledWrappedExecutor;

public class GuildMusicManager {

	public static final AudioPlayerManager AUDIO_PLAYER_MANAGER = new DefaultAudioPlayerManager();
	public static final ConcurrentHashMap<Snowflake, GuildMusic> GUILD_MUSIC_MAP = new ConcurrentHashMap<>();
	public static final ScheduledWrappedExecutor VOICE_LEAVE_SCHEDULER = new ScheduledWrappedExecutor("VoiceLeaveScheduler-%d");

	static {
		AudioSourceManagers.registerRemoteSources(AUDIO_PLAYER_MANAGER);
	}

	public static GuildMusic createGuildMusic(DiscordClient client, Snowflake guildId) {
		GuildMusic guildMusic = new GuildMusic(client, guildId, AUDIO_PLAYER_MANAGER);
		GUILD_MUSIC_MAP.put(guildId, guildMusic);
		return guildMusic;
	}

}
