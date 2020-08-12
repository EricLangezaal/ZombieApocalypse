package com.ericdebouwer.zombieapocalypse;

import org.bukkit.entity.Zombie;

public enum ZombieType{
	DEFAULT,
	SPRINTER,
	BOOMER,
	THROWER,
	TANK,
	NINJA,
	MULTIPLIER,
	JUMPER,
	PILLAR;
	
	private final static String ZOMBIE_INDENTIFIER = "ApocalypseZombieType";
	
	public static ZombieType getType(Zombie zombie){
		for (String tag: zombie.getScoreboardTags()){
			if (tag.startsWith(ZOMBIE_INDENTIFIER)){
				try {
					String type = tag.replaceFirst(ZOMBIE_INDENTIFIER, "");
					ZombieType result = ZombieType.valueOf(type);
					return result;
				}catch (IllegalArgumentException e){
					return ZombieType.DEFAULT;
				}
			}
		}
		return ZombieType.DEFAULT;
	}
	
	public static Zombie set(Zombie zombie, ZombieType zombieType){
		String type = ZOMBIE_INDENTIFIER + zombieType.toString();
		zombie.getScoreboardTags().add(type);
		return zombie;
	}
}

