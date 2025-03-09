package io.github.wall69.ancientnightmare.arena;

public enum ArenaState {

	WAITING("waiting"), COUNTDOWN("countdown"), PLAYING("playing"), ENDING("ending");

	private final String pathName;

	ArenaState(String pathName){
		this.pathName = pathName;
	}

	public String getPathName(){
		return pathName;
	}
}
