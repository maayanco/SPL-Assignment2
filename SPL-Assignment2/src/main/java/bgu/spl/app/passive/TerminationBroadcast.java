package bgu.spl.app.passive;

import bgu.spl.mics.Broadcast;

/**
 * Broadcast which indicates the recipient should terminate gracefully
 * 
 */
public class TerminationBroadcast implements Broadcast {

	private boolean shouldTerminate;

	/**
	 * @param toTerminate
	 *            - true if a termination should Occur, false otherwise.
	 */
	public TerminationBroadcast(boolean toTerminate) {
		this.shouldTerminate = toTerminate;
	}

	/**
	 * @return - true if a termination should occur, false otherwise.
	 */
	public boolean getTerminationStatus() {
		return shouldTerminate;
	}

}
