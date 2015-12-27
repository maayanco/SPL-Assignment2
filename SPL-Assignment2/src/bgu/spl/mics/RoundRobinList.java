package bgu.spl.mics;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RoundRobinList{
	
	private LinkedBlockingQueue<MicroService> list;
	private int index;
	
	public RoundRobinList(){
		list = new LinkedBlockingQueue<MicroService>();
		index=-1;
	}

	
	public int size() {
		return list.size();
	}

	
	public boolean isEmpty() {
		return list.isEmpty();
	}

	
	public boolean contains(Object o) {
		return list.contains(o);
	}

	
	public Iterator iterator() {
		return list.iterator();
	}

	

	public boolean add(Object e) {
		//If before the addition the list is empty than after the addition it will have an object - then we set the index as 0
		if(list.isEmpty()){
			index=0;
		}
		Boolean bool = list.add((MicroService)e);
		return bool;
	}

	public int getIndex(Object o){
		int i=0;
		for(MicroService m : list){
			if(m.equals(o))
				return i;
			i++;
		}
		
		return -1;
	}
	
	public MicroService getObjectAt(int index){
		int i=0;
		for(MicroService m : list){
			if(i==index)
				return m;
			i++;
		}
		return null;
	}
	
	
	public boolean remove(Object o) {
		int indexOfObj = getIndex(o); //ooooooh this is so! maybe not gonna work..!
		Boolean bool = list.remove((MicroService)o);
		if(list.isEmpty())
			index=-1;
		else if(bool){
			if(indexOfObj<index){
				index=(index-1)%list.size();
			}
			else{
				index=index%list.size(); 
			}
		}
		return bool;
	}
	
	private void updateIndex(int addition){
		index=(index+addition)%list.size();
	}


	public Object getNext(){
		MicroService obj = getObjectAt(index); //verrrrrrrrrry dangerous!!
		updateIndex(1);
		return obj;
	}
	
	public Object get(int index) {
		MicroService obj = getObjectAt(index); //veryyyyyyy dangerous!
		index=(index+1)%list.size();
		return obj;
	}

	
}