package jlibrtp;






public class Timer 
{
	private int timeout;
	private Signalable signalable;
	private Object lock=new Object();

	private Thread thread;
	
	public synchronized boolean isRunning() {
		return thread!=null && thread.isAlive();
	}
	
	public Timer(int timeout, Signalable signalable) {
		this.signalable=signalable;
		this.timeout=timeout;
	}

	private void createNewTimerThread() {
		thread=new Thread() {
			public void run()
			{
				try {
					System.out.println("preTimeout");
					Thread.sleep(timeout);
					System.out.println("postTimeout");
					
					signalable.signalTimeout();
					synchronized(lock) {
						lock.notifyAll();
					}
				} catch (InterruptedException e) {
					System.out.println("Timer thread was interrupted");
				}
			}
		};
	}
	
	
	public synchronized void startTimer() 
	{
		createNewTimerThread();
		thread.start();
	}
	
	
	public synchronized void stopTimer()
	{
		thread.interrupt();
	}
	
	
	public synchronized void resetTimer()
	{
		stopTimer();
		startTimer();
	}
}