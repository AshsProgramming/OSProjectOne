package nachos.threads;

import java.util.LinkedList;

public class Queue {
	protected LinkedList<Object> linked;
	Lock queueLock = new Lock();
	
	
	public Queue() {
		linked = new LinkedList<Object>();
	}
	public void add(KThread thread) {
		//make sure thread has the queue lock when we add it
		if(!queueLock.isHeldByCurrentThread()) {
			queueLock.acquire();
		}
		linked.add(thread);
		queueLock.release();
	}
	
	public void remove(KThread thread) {
		if(!queueLock.isHeldByCurrentThread()) {
			queueLock.acquire();
		}
		linked.remove(thread);
		queueLock.release();
	}
	
	public boolean isEmpty() {
		if(!queueLock.isHeldByCurrentThread()) {
			queueLock.acquire();
		}
		if(linked.size() == 0) {
			queueLock.release();
			return true;
		}else {
			queueLock.release();
			return false;
		}
	}
	public Object removeFirst() {
		if(!queueLock.isHeldByCurrentThread()) {
			queueLock.acquire();
		}
		Object removee = linked.removeFirst();
		queueLock.release();
		return removee;
	}
}
