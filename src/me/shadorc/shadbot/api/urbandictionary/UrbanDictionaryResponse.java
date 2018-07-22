package me.shadorc.shadbot.api.urbandictionary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UrbanDictionaryResponse {

	@JsonProperty("list")
	private List<UrbanDefinition> definitions;

	public List<UrbanDefinition> getDefinitions() {
		return definitions;
	}

	@Override
	public String toString() {
		return String.format("UrbanDictionaryResponse [definitions=%s]", definitions);
	}

}
