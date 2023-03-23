package nachos.threads;

import nachos.machine.*;
import nachos.threads.MinHeap;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
	public MinHeap myHeap = new MinHeap();
    public Alarm() {
    	
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    //TODO
    public void timerInterrupt() {
    	if(myHeap.size()>0) {
    		while(myHeap.firstKey()<=Machine.timer().getTime()) {
    			KThread wakeThread = new KThread();
    			boolean intStatus = Machine.interrupt().disable();
    			wakeThread = (KThread)myHeap.removeFirst();
    			wakeThread.ready();
    			if(myHeap.size()<=0) {
    				break;
    			}
    			Machine.interrupt().restore(intStatus);
    		}
    	}
        
       KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    //TODO
    public void waitUntil(long x) {
    	long wakeTime = Machine.timer().getTime() + x;
    	boolean intStat = Machine.interrupt().disable(); //interrupt status

    	
    	myHeap.add(wakeTime,KThread.currentThread()); //add the thread and time to myHeap
    	KThread.currentThread().sleep();
    	
    	Machine.interrupt().restore(intStat);//interrupt status will wake up the thread after the timer is up
    }
}
