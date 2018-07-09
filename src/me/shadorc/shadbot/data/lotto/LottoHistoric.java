package me.shadorc.shadbot.data.lotto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LottoHistoric {

	@JsonProperty("jackpot")
	private int jackpot;
	@JsonProperty("winners_count")
	private int winnersCount;
	@JsonProperty("number")
	private int number;

	public int getJackpot() {
		return jackpot;
	}

	public int getWinnersCount() {
		return winnersCount;
	}

	public int getNumber() {
		return number;
	}

}
