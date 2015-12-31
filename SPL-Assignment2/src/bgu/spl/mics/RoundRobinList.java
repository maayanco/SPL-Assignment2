package bgu.spl.mics;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class represents a list which operates in a round robin manner.
 */
public class RoundRobinList {

	private LinkedBlockingQueue<MicroService> list;
	private int index;

	/**
	 * Initializes the list and index
	 */
	public RoundRobinList() {
		list = new LinkedBlockingQueue<MicroService>();
		index = -1;
	}

	/**
	 * @return the size of the list
	 */
	public int size() {
		return list.size();
	}

	/**
	 * @return true if the list is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * 
	 * @param m
	 *            - micro service that may be inside the list or not
	 * @return true if the list contains m, false otherwise
	 */
	public boolean contains(MicroService m) {
		return list.contains(m);
	}

	/**
	 * @return an iterator on the list
	 */
	@SuppressWarnings("rawtypes")
	public Iterator iterator() {
		return list.iterator();
	}

	/**
	 * 
	 * @param m
	 *            - micro service to be added to the list
	 * @return true if m was successfully added to the list
	 */
	public boolean add(MicroService m) {
		if (list.isEmpty()) {
			index = 0;
		}
		Boolean bool = list.add((MicroService) m);
		return bool;
	}

	/**
	 * 
	 * @param m
	 *            - the micro service we will return it's index if it exists in
	 *            the list
	 * @return the index of the microService m, or -1 if not found in the list.
	 */
	public int getIndex(MicroService m) {
		int i = 0;
		for (MicroService microService : list) {
			if (microService.equals(m))
				return i;
			i++;
		}

		return -1;
	}

	/**
	 * @param index
	 *            - the index of the micro service to be returned
	 * @return the micro service in the provided index, if exists such
	 */
	public MicroService getObjectAt(int index) {
		int i = 0;
		for (MicroService m : list) {
			if (i == index)
				return m;
			i++;
		}
		return null;
	}

	/**
	 * 
	 * @param m
	 *            - the micro service to be removed
	 * @return true if the removal succeeded
	 */
	public boolean remove(MicroService m) {
		int indexOfObj = getIndex(m);
		Boolean bool = list.remove((MicroService) m);
		if (list.isEmpty())
			index = -1;
		else if (bool) {
			if (indexOfObj < index) {
				index = (index - 1) % list.size();
			} else {
				index = index % list.size();
			}
		}
		return bool;
	}

	/**
	 * @param addition
	 *            - the amount to be added to the current index
	 */
	private void updateIndex(int addition) {
		index = (index + addition) % list.size();
	}

	/**
	 * @return the next micro service in the list according to the round robin
	 *         algorithm
	 */
	public Object getNext() {
		MicroService obj = getObjectAt(index);
		updateIndex(1);
		return obj;
	}

	/**
	 * 
	 * @param index
	 *            the index of the micro service we want to retreive
	 * @return the micro service at the requested index if exists
	 */
	public Object get(int index) {
		MicroService obj = getObjectAt(index);
		index = (index + 1) % list.size();
		return obj;
	}

}