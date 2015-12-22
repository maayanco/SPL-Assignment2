package bgu.spl.app;

import bgu.spl.mics.Broadcast;

//lital
public class TickBroadcast implements Broadcast {
	
	private int tick;
	
	public  TickBroadcast(int tick){
		this.tick=tick;
	}
	
	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}
}
