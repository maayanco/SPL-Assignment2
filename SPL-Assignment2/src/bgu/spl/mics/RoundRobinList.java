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

	
	//i want to implement round robin, which means every time i receive a get request i return the value thats in the
	//index i have
	//and when the size of my list is 1 then we need to set the index to 
	
	
	public int size() {
		// TODO Auto-generated method stub
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

	/*@Override
	public Object[] toArray() {
		return list.toArray();
	}*/

	/*@Override
	public Object[] toArray(Object[] a) {
		return list.toArray(a);
	}*/


	public boolean add(Object e) {
		//If before the addition the list is empty than after the addition it will have an object - then we set the index as 0
		if(list.isEmpty())
			index=0;
		Boolean bool = list.add((MicroService)e);
		return bool;
	}

	
	public boolean remove(Object o) {
		Boolean bool = list.remove((MicroService)o);
		if(bool)
			updateIndex(0);
		return bool;
	}
	
	private void updateIndex(int addition){
		index=(index+addition)%list.size();
	}

	/*@Override
	public boolean containsAll(Collection c) {
		return list.containsAll(c);
	}*/

	/*@Override
	public boolean addAll(Collection c) {
		return list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection c) {
		return list.addAll(index,c);
	}

	@Override
	public boolean removeAll(Collection c) {
		return list.removeAll(c);
	}
*/
	/*@Override
	public boolean retainAll(Collection c) {
		return list.retainAll(c);
	}

	@Override
	public void clear() {
		list.clear();
		
	}*/

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
/*
	@Override
	public Object set(int index, Object element) {
		return list.set(index, (MicroService)element);
	}*/

	/*@Override
	public void add(int index, Object element) {
		list.add(index,(MicroService)element);
	}*/

	/*@Override
	public Object remove(int index) {
		return list.remove(index);
	}
*/
	/*@Override
	public int indexOf(Object o) {
		return list.indexOf((MicroService)o);
	}*/

	/*@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf((MicroService)o);
	}

	@Override
	public ListIterator listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return list.listIterator();
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	*/
	
}