package net.labymod.serverapi.discord;

import java.util.Observable;
import java.util.Observer;

import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RichPresence extends Observable implements Observer
{
	private String matchSecret;
	private String spectateSecret;
	private String joinSecrert;
	private Game game;
	private Party party;
	
	public void setMatchSecret(String matchSecret)
	{
		this.matchSecret = matchSecret;
		setChanged();
		notifyObservers();
	}

	public void setSpecrateSecret(String specrateSecret)
	{
		this.spectateSecret = specrateSecret;
		setChanged();
		notifyObservers();
	}

	public void setJoinSecrert(String joinSecrert)
	{
		this.joinSecrert = joinSecrert;
		setChanged();
		notifyObservers();
	}

	public void setParty(Party party)
	{
		this.party = party;
		setChanged();
		notifyObservers();
	}
	
	@Override
	public void update(Observable o, Object arg)
	{
		setChanged();
		notifyObservers();
	}
	
	public JsonObject toJson()
	{
		JsonObject object = new JsonObject();
		
		addSecretsToJson(object);
		addGameToJson(object);
		addPartyToJson(object);
		
		return object;
	}
	
	private void addSecretsToJson(JsonObject object)
	{
		object.addProperty("hasMatchSecret", matchSecret != null);
		if(matchSecret != null)
		{
			object.addProperty("matchSecret", matchSecret);
		}
		
		object.addProperty("hasSpectateSecret", spectateSecret != null);
		if(spectateSecret != null)
		{
			object.addProperty("spectateSecret", spectateSecret);
		}
		
		object.addProperty("hasJoinSecret", joinSecrert != null);
		if(spectateSecret != null)
		{
			object.addProperty("joinSecret", joinSecrert);
		}
	}
	
	private void addGameToJson(JsonObject object)
	{
		object.addProperty("hasGame", game != null);
		if(game != null)
		{
			object.addProperty("game_mode", game.getGameMode());
			object.addProperty("game_startTime", game.getStartTime() != null ? game.getStartTime().toEpochMilli() : 0);
			object.addProperty("game_endTime", game.getEndTime() != null ? game.getEndTime().toEpochMilli() : 0);
		}
	}
	
	private void addPartyToJson(JsonObject object)
	{
		object.addProperty("hasParty", party != null);
		if(party != null)
		{
			object.addProperty("partyId", party.getPartyId());
			object.addProperty("party_size", party.getPartySize());
			object.addProperty("party_max", party.getPartyMax());
		}
	}
}
