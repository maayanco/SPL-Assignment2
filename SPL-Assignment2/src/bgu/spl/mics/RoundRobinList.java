package bgu.spl.mics;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class RoundRobinList{
	
	private LinkedList<MicroService> list;
	private int index;
	
	public RoundRobinList(){
		list = new LinkedList<MicroService>();
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

	
	public boolean remove(Object o) {
		int indexOfObj = list.indexOf(o);
		Boolean bool = list.remove((MicroService)o);
		if(bool){
			if(indexOfObj<index)
				index=(index-1)%list.size();
			else
				index=index%list.size();
		}
		return bool;
	}
	
	private void updateIndex(int addition){
		index=(index+addition)%list.size();
	}


	public Object getNext(){
		Object obj= list.get(index);
		updateIndex(1);
		return obj;
	}
	
	public Object get(int index) {
		Object obj = list.get(index);
		index=(index+1)%list.size();
		return obj;
	}

	
}