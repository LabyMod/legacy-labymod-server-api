package net.labymod.serverapi.discord;

import java.io.Closeable;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import com.google.gson.JsonObject;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RichPresence extends Observable implements Observer, Closeable {
	private String matchSecret;
	private String spectateSecret;
	private String joinSecrert;
	private Game game;
	private Party party;
	
	public void setMatchSecret(String matchSecret) {
		this.matchSecret = matchSecret;
		setChanged();
		notifyObservers();
	}

	public void setSpectateSecret(String spectateSecret) {
		this.spectateSecret = spectateSecret;
		setChanged();
		notifyObservers();
	}

	public void setJoinSecrert(String joinSecret) {
		this.joinSecrert = joinSecret;
		setChanged();
		notifyObservers();
	}

	public void setParty(Party party) {
		if(this.party != null) {
			this.party.deleteObserver(this);
		}
		
		this.party = party;
		
		if(party != null) {
			party.addObserver(this);
		}
		
		setChanged();
		notifyObservers();
	}
	
	public void setGame(Game game) {
		if(this.game != null) {
			this.game.deleteObserver(this);
		}
		
		this.game = game;
		
		if(game != null) {
			game.addObserver(this);
		}	
		
		setChanged();
		notifyObservers();
	}
	
	@Override
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}
	
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		
		addSecretsToJson(object);
		addGameToJson(object);
		addPartyToJson(object);
		
		return object;
	}
	
	private void addSecretsToJson(JsonObject object) {
		object.addProperty("hasMatchSecret", matchSecret != null);
		if(matchSecret != null) {
			object.addProperty("matchSecret", matchSecret);
		}
		
		object.addProperty("hasSpectateSecret", spectateSecret != null);
		if(spectateSecret != null) {
			object.addProperty("spectateSecret", spectateSecret);
		}
		
		object.addProperty("hasJoinSecret", joinSecrert != null);
		if(spectateSecret != null) {
			object.addProperty("joinSecret", joinSecrert);
		}
	}
	
	private void addGameToJson(JsonObject object) {
		object.addProperty("hasGame", game != null);
		if(game != null) {
			object.addProperty("game_mode", game.getGameMode());
			object.addProperty("game_startTime", game.getStartTime() != null ? game.getStartTime().toEpochMilli() : 0);
			object.addProperty("game_endTime", game.getEndTime() != null ? game.getEndTime().toEpochMilli() : 0);
		}
	}
	
	private void addPartyToJson(JsonObject object) {
		object.addProperty("hasParty", party != null);
		if(party != null) {
			object.addProperty("partyId", party.getPartyId());
			object.addProperty("party_size", party.getPartySize());
			object.addProperty("party_max", party.getPartyMax());
		}
	}
	
	public void destroy() {
		deleteObservers();
		setGame(null);
		setParty(null);
	}

	@Override
	public void close() throws IOException {
		destroy();
	}
}
