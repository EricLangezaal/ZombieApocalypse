package com.ericdebouwer.zombieapocalypse.zombie;

import org.bukkit.entity.Zombie;
import javax.annotation.Nullable;

public enum ZombieType {
	DEFAULT,
	SPRINTER,
	BOOMER,
	THROWER,
	TANK,
	NINJA,
	MULTIPLIER,
	JUMPER,
	PILLAR;
	
	private final static String ZOMBIE_IDENTIFIER = "ApocalypseZombieType";

	/**
	 * Get the type of a zombie, or null if doesn't have one
	 *
	 * @return the type of a zombie, or null
	 */
	public static @Nullable ZombieType getType(Zombie zombie){
		for (String tag: zombie.getScoreboardTags()){
			if (tag.startsWith(ZOMBIE_IDENTIFIER)){
				try {
					String type = tag.replaceFirst(ZOMBIE_IDENTIFIER, "");
					return ZombieType.valueOf(type);
				}catch (IllegalArgumentException e){
					return ZombieType.DEFAULT;
				}
			}
		}
		return null;
	}

	public Zombie set(Zombie zombie){
		String type = ZOMBIE_IDENTIFIER + this;
		zombie.getScoreboardTags().add(type);
		return zombie;
	}
}

