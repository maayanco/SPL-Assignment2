package bgu.spl.app;

import bgu.spl.mics.Broadcast;

//lital
public class TickBroadcast implements Broadcast {
	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	private int  tick;
	
	public  TickBroadcast(int tick){
		this.tick=tick;
		
	}

}
