package net.labymod.serverapi.discord;

import java.time.Instant;
import java.util.Observable;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Builder
@Getter
@Setter
public class Game extends Observable
{
	@NonNull
	private String gameMode;
	private Instant startTime;
	private Instant endTime;
}
