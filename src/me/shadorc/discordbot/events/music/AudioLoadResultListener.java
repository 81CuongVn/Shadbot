package me.shadorc.discordbot.events.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.jsoup.Jsoup;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.shadorc.discordbot.data.Config;
import me.shadorc.discordbot.data.Setting;
import me.shadorc.discordbot.data.Storage;
import me.shadorc.discordbot.message.MessageListener;
import me.shadorc.discordbot.message.MessageManager;
import me.shadorc.discordbot.music.GuildMusicManager;
import me.shadorc.discordbot.utils.BotUtils;
import me.shadorc.discordbot.utils.LogUtils;
import me.shadorc.discordbot.utils.StringUtils;
import me.shadorc.discordbot.utils.command.Emoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;

public class AudioLoadResultListener implements AudioLoadResultHandler, MessageListener {

	public static final String YT_SEARCH = "ytsearch: ";
	public static final String SC_SEARCH = "scsearch: ";

	private static final int CHOICE_DURATION = 30;

	private final GuildMusicManager musicManager;
	private final IUser userDj;
	private final IVoiceChannel userVoiceChannel;
	private final String identifier;

	private List<AudioTrack> resultsTracks;
	private Timer cancelTimer;

	public AudioLoadResultListener(GuildMusicManager musicManager, IUser userDj, IVoiceChannel userVoiceChannel, String identifier) {
		this.musicManager = musicManager;
		this.userDj = userDj;
		this.userVoiceChannel = userVoiceChannel;
		this.identifier = identifier;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		musicManager.joinVoiceChannel(userVoiceChannel, false);
		if(musicManager.getScheduler().isPlaying()) {
			BotUtils.sendMessage(Emoji.MUSICAL_NOTE + " **"
					+ StringUtils.formatTrackName(track.getInfo()) + "** has been added to the playlist.", musicManager.getChannel());
		}
		musicManager.getScheduler().queue(track);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		// SoundCloud send empty playlist when no result are found
		if(playlist.getTracks().isEmpty()) {
			this.onNoMatches();
			return;
		}

		List<AudioTrack> tracks = playlist.getTracks();

		if(identifier.startsWith(YT_SEARCH) || identifier.startsWith(SC_SEARCH)) {
			if(MessageManager.isWaitingForMessage(musicManager.getChannel())) {
				BotUtils.sendMessage(Emoji.HOURGLASS + " Someone is already selecting a music,"
						+ " please wait for him to finish.", musicManager.getChannel());
				return;
			}

			musicManager.setDj(userDj);

			StringBuilder strBuilder = new StringBuilder();
			for(int i = 0; i < Math.min(5, tracks.size()); i++) {
				strBuilder.append("\n\t**" + (i + 1) + ".** " + StringUtils.formatTrackName(tracks.get(i).getInfo()));
			}

			EmbedBuilder embed = new EmbedBuilder()
					.withAuthorName("Results (Use " + Storage.getGuild(musicManager.getChannel().getGuild()).getSetting(Setting.PREFIX) + "cancel "
							+ "to cancel the selection)")
					.withAuthorIcon(musicManager.getDj().getAvatarURL())
					.withColor(Config.BOT_COLOR)
					.withThumbnail("http://icons.iconarchive.com/icons/dtafalonso/yosemite-flat/512/Music-icon.png")
					.appendDescription("**Select a music by typing the corresponding number.**"
							+ "\nYou can choose several musics by separating them with a comma."
							+ "\nExample: 1,3,4"
							+ "\n" + strBuilder.toString())
					.withFooterText("This choice will be canceled in " + CHOICE_DURATION + " seconds.");
			BotUtils.sendMessage(embed.build(), musicManager.getChannel());

			cancelTimer = new Timer((int) TimeUnit.SECONDS.toMillis(CHOICE_DURATION), event -> {
				this.stopWaiting();
			});
			cancelTimer.start();

			resultsTracks = tracks;
			MessageManager.addListener(musicManager.getChannel(), this);
			return;
		}

		musicManager.joinVoiceChannel(userVoiceChannel, false);

		tracks.stream().limit(Config.MAX_PLAYLIST_SIZE).forEach(track -> musicManager.getScheduler().queue(track));

		BotUtils.sendMessage(Emoji.MUSICAL_NOTE + " " + musicManager.getScheduler().getPlaylist().size()
				+ " musics have been added to the playlist.", musicManager.getChannel());
	}

	@Override
	public void loadFailed(FriendlyException err) {
		String errMessage = Jsoup.parse(err.getMessage().replace("Watch on YouTube", "")).text().trim();
		if(err.severity.equals(FriendlyException.Severity.FAULT)) {
			LogUtils.warn("{Guild ID: " + musicManager.getChannel().getGuild().getLongID() + "} "
					+ "Load failed, Shadbot might be able to continue playing: " + errMessage);
		} else {
			BotUtils.sendMessage(Emoji.RED_CROSS + " Sorry, " + errMessage.toLowerCase(), musicManager.getChannel());
			LogUtils.info("{Guild ID: " + musicManager.getChannel().getGuild().getLongID() + "} Load failed: " + errMessage);
		}

		if(musicManager.getScheduler().isStopped()) {
			musicManager.leaveVoiceChannel();
		}
	}

	@Override
	public void noMatches() {
		this.onNoMatches();
	}

	private void onNoMatches() {
		BotUtils.sendMessage(Emoji.MAGNIFYING_GLASS + " No result for \""
				+ identifier.replaceAll(YT_SEARCH + "|" + SC_SEARCH, "") + "\"", musicManager.getChannel());
		LogUtils.info("{Guild ID: " + musicManager.getChannel().getGuild().getLongID() + "} No matches: " + identifier);

		if(musicManager.getScheduler().isStopped()) {
			musicManager.leaveVoiceChannel();
		}
	}

	@Override
	public boolean onMessageReceived(IMessage message) {
		if(!message.getAuthor().equals(musicManager.getDj())) {
			return false;
		}

		String prefix = (String) Storage.getGuild(musicManager.getChannel().getGuild()).getSetting(Setting.PREFIX);
		if(message.getContent().equalsIgnoreCase(prefix + "cancel")) {
			BotUtils.sendMessage(Emoji.CHECK_MARK + " Choice canceled.", musicManager.getChannel());
			this.stopWaiting();
			return true;
		}

		String content = message.getContent();

		List<Integer> choices = new ArrayList<>();
		for(String str : content.split(",")) {
			// Remove all non numeric characters
			String numStr = str.replaceAll("[^\\d]", "");
			if(!StringUtils.isPositiveInt(numStr)) {
				this.sendInvalidChoice(str.trim(), prefix, message);
				return true;
			}

			int num = Integer.parseInt(numStr);
			if(num < 1 || num > Math.min(5, resultsTracks.size())) {
				this.sendInvalidChoice(str.trim(), prefix, message);
				return true;
			}

			if(!choices.contains(num)) {
				choices.add(num);
			}
		}

		// If the manager was removed from the list while an user chose a music, we re-add it and join voice channel
		GuildMusicManager.putGuildMusicManagerIfAbsent(message.getGuild(), musicManager);
		musicManager.joinVoiceChannel(userVoiceChannel, false);

		for(int choice : choices) {
			AudioTrack track = resultsTracks.get(choice - 1);
			if(musicManager.getScheduler().isPlaying()) {
				BotUtils.sendMessage(Emoji.MUSICAL_NOTE + " **" + StringUtils.formatTrackName(track.getInfo())
						+ "** has been added to the playlist.", musicManager.getChannel());
			}
			musicManager.getScheduler().queue(track);
		}

		this.stopWaiting();
		return true;
	}

	private void stopWaiting() {
		cancelTimer.stop();
		resultsTracks.clear();
		MessageManager.removeListener(musicManager.getChannel());

		if(musicManager.getScheduler().isStopped()) {
			musicManager.leaveVoiceChannel();
		}
	}

	private void sendInvalidChoice(String choice, String prefix, IMessage message) {
		BotUtils.sendMessage(Emoji.GREY_EXCLAMATION + " Music selection: \"" + choice + "\" is not a valid number."
				+ " Enter a number between 1 and " + Math.min(5, resultsTracks.size()) + " or use `" + prefix + "cancel` to cancel the selection.",
				musicManager.getChannel());
		LogUtils.info("{Guild ID: " + musicManager.getChannel().getGuild().getLongID() + "} Invalid choice: " + message.getContent());
	}
}
