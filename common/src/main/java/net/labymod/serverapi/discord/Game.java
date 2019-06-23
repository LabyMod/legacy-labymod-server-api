package net.labymod.serverapi.discord;

import java.time.Instant;
import java.util.Observable;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class Game extends Observable
{
	@NonNull
	private String gameMode;
	private Instant startTime;
	private Instant endTime;
	
	public void setGameMode(String gameMode)
	{
		this.gameMode = gameMode;
		setChanged();
		notifyObservers();
	}
	public void setStartTime(Instant startTime)
	{
		this.startTime = startTime;
		setChanged();
		notifyObservers();
	}
	public void setEndTime(Instant endTime)
	{
		this.endTime = endTime;
		setChanged();
		notifyObservers();
	}
}
