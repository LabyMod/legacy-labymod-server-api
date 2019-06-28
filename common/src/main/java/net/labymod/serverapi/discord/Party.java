package net.labymod.serverapi.discord;

import java.util.Observable;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class Party extends Observable {
	@NonNull
	private String partyId;
	private int partySize;
	private int partyMax;
	
	public void setPartyId(String partyId) {
		this.partyId = partyId;
		setChanged();
		notifyObservers();
	}

	public void setPartySize(int partySize) {
		this.partySize = partySize;
		setChanged();
		notifyObservers();
	}

	public void setPartyMax(int partyMax) {
		this.partyMax = partyMax;
		setChanged();
		notifyObservers();
	}
}
