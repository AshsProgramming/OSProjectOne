package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
	private Lock conditionLock;
    private static Queue sleepingQueue = new Queue();
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	Machine.interrupt().disable();		//no interrupt while sleeping
    	
    	conditionLock.release();			//locks release when sleeping

    	KThread thread = KThread.currentThread();	//current thread
    	sleepingQueue.add(thread);			// add to sleeping queue
    	KThread.currentThread().sleep();							//put it to sleep sleeps
    	
    	conditionLock.acquire();					//re-lock 
    	Machine.interrupt().enable();				//interrupt is okay
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	if(!sleepingQueue.isEmpty()) {
    		boolean intStatus = Machine.interrupt().disable();	
    		KThread thread = (KThread) sleepingQueue.removeFirst();
    		thread.ready();
    		Machine.interrupt().restore(intStatus);
    	}
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	while(!sleepingQueue.isEmpty()) {
			wake();
    	}

    }
    
}
