package com.ericdebouwer.zombieapocalypse;


public class ApocalypseWorld {

	public String worldName;
	public long endTime;
	
	public int mobcap;
	
	public ApocalypseWorld(String worldName, long endTime, int mobCap){
		this.worldName = worldName;
		this.endTime = endTime;
		this.mobcap = mobCap;
	}
	
	public void setMobCap(int mobCap){
		this.mobcap = mobCap;
	}

}
