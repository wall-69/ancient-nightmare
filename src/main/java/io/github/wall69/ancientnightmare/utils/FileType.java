package io.github.wall69.ancientnightmare.utils;

public enum FileType {

	CONFIG("config", 0), ARENAS("arenas", 1), LANGUAGE("language", 2),
	PLAYER_STATS("player_stats", 3);

	private final String name;
	private final int id;

	FileType(String name, int id) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}
}
