package nachos.threads;


import nachos.ag.BoatGrader;


public class Boat
{
    static BoatGrader bg;
    
    
    //we keep a count of the number of children on oahu, 
    private static int childrenOnOahu; 
    //the number of adults on each Oahu, 
    private static int adultsOnOahu;
    //the number of children ready to go to Molokai
    private static int childrenOnBoat;
     //the number of children (0, 1, or 2) who just rowed together from Oahu to Molokai
    private static int childrenFromBoat;
    //boolean as to if the boat is on Oahu or not
    private static boolean dockedAtOahu;

    //island locks; supports conditions to lock island so one thread accesses it at a time
    private static Lock oahu = new Lock();
    private static Lock molokai = new Lock();

    //condition for adults;
    //	if there are 2 or more children on the island then sleep
    private static Condition2 soloPilot = new Condition2(oahu);
    
    //three conditions for children:
    // sleeping on Molokai, will wake up for an adult needing the boat back to oahu
    private static Condition childWaitingToGoBackToOahu = new Condition(molokai); 
    //Child threads sleep in Oahu if there are already more than 2 children ready to go over
    private static Condition childWaitingOnOahu = new Condition(oahu);
    //Sleeping on boat while the child waits for the second passenger
    private static Condition childWaitingInBoatInOahu = new Condition(oahu);



    //the main thread waits until our code says it's finished
    private static Semaphore finished = new Semaphore(0);
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;
	System.out.println("Adults: "+adults+" Children: "+children);
	// Instantiate global variables here
	childrenOnOahu = children;
	adultsOnOahu = adults;
	
	childrenOnBoat = 0;
	childrenFromBoat = 0;
	
	dockedAtOahu = true;
	
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	Runnable Adult = new Runnable() {
	    public void run() {
                AdultItinerary();
            }
        };	
    Runnable Child = new Runnable() {
	    public void run() {
                ChildItinerary();
            }
        };
        
       //make adults
       for(int i=0;i<adultsOnOahu;i++) {
    	   KThread t = new KThread(Adult);
    	   t.setName("Adult "+i);
    	   t.fork();
       }
       for(int c=0;c<childrenOnOahu;c++) {
    	   KThread t = new KThread(Child);
    	   t.setName("Child "+c);
    	   t.fork();
       }
       finished.P();
    }
    
    //TODO Project Two
    //TASK 1: Synchronization
    
    
/* 		Adult Itinerary on Oahu to Molokai
     * 	If on Oahu, make sure the adult waits until less than 2 children are on Oahu
     * 		 the adult sails to Molokai
     *  A child will pilot BACK to Oahu for any adults or the last child
     */
    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	   is ran when an adult rows children to Molokai
	*/
    	
    	turnTheKey(oahu);
    	//makes sure the children go first and there is a boat to go on
    	while(childrenOnOahu > 1 || !dockedAtOahu) {
    		soloPilot.sleep(); //The child bringing back the boat will wake them up
    	}

    	adultsOnOahu-=1; //Adult is leaving Oahu
    	dockedAtOahu = false;
    	
    	//adult is ready and waited until children were ready

    	bg.AdultRowToMolokai(); //Let's f'in ROOOOW

    	turnTheKey(oahu);
    	
    	
    	//now we should be on Molokai
    	turnTheKey(molokai);
    	//now its in the child pilot's (little) hands to bring the boat back to Oahu
    	childWaitingToGoBackToOahu.wake();
    	//since we signaled the child pilot, release the hold on Molokai
    	turnTheKey(molokai);
  
    }

    /* 								Child Itinerary
     * 		 Children will board the boat if there is a population on the island
     * 		   ((which will wake the adult waiting to pilot if there is one))
     *  	 	 It will wake the adult waiting IF there is only one child 
     *  	    	since two is the min AND max child passenger list
     *  Since Child Itinerary is ran by the child, it goes on assuming there's another child
     *  It checks if there is another child waiting on the boat already, if not, they assume that role. 
     *  	This child will wake up a child sleeping on land if there is one to run with them,
     *  	Goes to sleep until someone becomes the pilot.
     *  If this is the third passenger and second child, they will wake up the pilot so the adult will row FIRST
     * 	 	 Then, they wake up the child on the boat so that thread will go. Then, child rows to Molokai
     *  	One child is going to go back to Oahu so adults can come over to the island, Adults shouldn't leave Molokai
     */
    static void ChildItinerary()
    {
    	
    	while(OahuPopulation()>0) { 
    		
    		//lock the island we're on
    		turnTheKey(oahu);
    		
    		while(!dockedAtOahu || childrenOnBoat>=2) { //children waiting to go will sleep on Oahu

    			childWaitingOnOahu.sleep();
    		}    
    		
    		
    		if(wouldChildAndAdultBeAloneTogether()){
    			soloPilot.wake();
    		}
    		
    		if(childrenOnBoat == 0) { //there is no one on board then you'll be the pilot
    			
    			childrenOnBoat+=1; //we are now waiting 
    			
    			childWaitingOnOahu.wake(); //see if there is someone who can board
    			
    			childWaitingInBoatInOahu.sleep();
    			

    			bg.ChildRideToMolokai();
    			System.out.println("Both have arrived!");
    			
    			childWaitingInBoatInOahu.wake();

    		}else { //This child is the passenger
    			
    			childrenOnBoat+=1; //up the boat population for the pilot

    			//let them know they can go
    			childWaitingInBoatInOahu.wake();

    			bg.ChildRowToMolokai();
    			
    			
    			childWaitingInBoatInOahu.sleep();
    			
       		}
    		
    		//RAN BY ALL CHILDREN ONCE OFF THE BOAT IN MOLOKAI
    		System.out.println("Child is on Molokai");
    		//reset the oahu variables
    		dockedAtOahu = false;
    		childrenOnBoat-=1;
    		childrenOnOahu-=1;
    		
    		turnTheKey(oahu); //unlock oahu
    		turnTheKey(molokai); //lock molokai
    		
    		//set the molokai variables

    		childrenFromBoat+=1;
    		if(childrenFromBoat == 1) { //the first child that made it to molokai goes back for adults
    			childWaitingToGoBackToOahu.sleep();
    		}
    		//The unlucky one that gets to go back to Oahu
    		childrenFromBoat = 0;
    		turnTheKey(molokai);
    		
    		System.out.println("child about to row back");
    		bg.ChildRowToOahu();
    		
    		turnTheKey(oahu);
    		
    		childrenOnOahu += 1;
    		dockedAtOahu = true;
   
    		//since we turn the key at the beginning of the loop, unlock for resources
    		turnTheKey(oahu);
    	}
    	//should only be one child left, and may have rowed back to an empty island
    	//we should let them go back 
    	turnTheKey(oahu);
    	childrenOnOahu -= 1; //remove us from this 
    	turnTheKey(oahu);
    	
    	bg.ChildRowToMolokai();
    	
    	System.out.println("Finished");
    	
    	//Who's on Oahu? If any?
    	finished.V();	
    }
  
    
    /*
     * Functions to help support the itinerary logic
     */
    
    
    /**
     * Toggles the lock, (like turning a key; hence the name)
     * 	Acquires if the lock is not held by thread
     * @param lock
     */
    static void turnTheKey(Lock lock) {
    	if(lock.isHeldByCurrentThread()) {
    		lock.release();
    	}else {
    		lock.acquire();
    	}
    }
    
    /**
     * Oahu Population Function, uses global static variables
     * subtracts one to not count the child calling this
     * @return the population of Oahu
     */
    public static int OahuPopulation() {
    	return childrenOnOahu + adultsOnOahu - 1;
    }
    
    /**
     * Conditional Function to check if the adult needs to row alone 
     * 	since one child and one adult cannot row alone
     * @return true if children equals 1
     */
    
    static boolean wouldChildAndAdultBeAloneTogether() {
    	return childrenOnOahu==1;
    }


    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");


    }
    
    public static void main(String[]args) {
    	
    	selfTest();
    }
    
}


