package bgu.spl.app;
import bgu.spl.mics.Broadcast;

public class TerminationBroadcast implements Broadcast{
	private boolean toTerminate;
	
	public TerminationBroadcast(boolean toTerminate){
		this.toTerminate=toTerminate;
	}
	
	public boolean getToTerminate(){
		return toTerminate;
	}
	
}

