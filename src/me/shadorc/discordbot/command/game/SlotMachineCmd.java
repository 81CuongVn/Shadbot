package me.shadorc.discordbot.command.game;

import java.awt.Color;

import me.shadorc.discordbot.Emoji;
import me.shadorc.discordbot.command.Command;
import me.shadorc.discordbot.command.Context;
import me.shadorc.discordbot.utils.BotUtils;
import me.shadorc.discordbot.utils.Utils;
import sx.blah.discord.util.EmbedBuilder;

public class SlotMachineCmd extends Command {

	private enum SlotOptions {
		CHERRIES,
		BELL,
		GIFT
	}

	private static final int PAID_COST = 5;
	private static final SlotOptions[] slotsArray = new SlotOptions[] {
			SlotOptions.CHERRIES, SlotOptions.CHERRIES, SlotOptions.CHERRIES, SlotOptions.CHERRIES, //Winning chance : 12.5%
			SlotOptions.BELL, SlotOptions.BELL, SlotOptions.BELL, 									//Winning chance : 5.3%
			SlotOptions.GIFT}; 																		//Winning chance : 0.2%

	public SlotMachineCmd() {
		super(false, "machine_sous", "slot_machine");
	}

	@Override
	public void execute(Context context) {
		if(context.getUser().getCoins() < PAID_COST) {
			BotUtils.sendMessage(Emoji.BANK + " Vous n'avez pas assez de coins pour jouer à la machine à sous, une partie coûte " + PAID_COST + ".", context.getChannel());
			return;
		}

		SlotOptions slot1 = slotsArray[Utils.rand(slotsArray.length)];
		SlotOptions slot2 = slotsArray[Utils.rand(slotsArray.length)];
		SlotOptions slot3 = slotsArray[Utils.rand(slotsArray.length)];

		int gains = -PAID_COST;

		if(slot1 == SlotOptions.CHERRIES && slot2 == SlotOptions.CHERRIES && slot3 == SlotOptions.CHERRIES) {
			gains = 30;
		}
		else if(slot1 == SlotOptions.BELL && slot2 == SlotOptions.BELL && slot3 == SlotOptions.BELL) {
			gains = 150;
		}
		else if(slot1 == SlotOptions.GIFT && slot2 == SlotOptions.GIFT && slot3 == SlotOptions.GIFT) {
			gains = 5000;
		}
		context.getUser().addCoins(gains);

		StringBuilder message = new StringBuilder();
		message.append(":" + slot1.toString().toLowerCase() + ": :" + slot2.toString().toLowerCase() + ": :" + slot3.toString().toLowerCase() + ":");
		message.append("\nVous avez "+ (gains > 0 ? "gagné" : "perdu") + " " + Math.abs(gains) + " coins !");
		BotUtils.sendMessage(message.toString(), context.getChannel());
	}

	@Override
	public void showHelp(Context context) {
		EmbedBuilder builder = new EmbedBuilder()
				.withAuthorName("Aide pour la commande /" + context.getArg())
				.withAuthorIcon(context.getClient().getOurUser().getAvatarURL())
				.withColor(new Color(170, 196, 222))
				.appendDescription("**Joue à la machine à sous pour " + PAID_COST + " coins.**")
				.appendField("Gains", "Vous avez 12.5% de chance de gagner 30 coins, 5.3% de gagner 150 coins et 0.2% de gagner 5000 coins.", false);
		BotUtils.sendEmbed(builder.build(), context.getChannel());
	}
}
