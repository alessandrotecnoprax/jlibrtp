/**
 * Java RTP Library
 * Copyright (C) 2006 Vaishnav Janardhan
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jlibrtp;

/**
 * Timer
 * 
 * @author Vaishnav Janardhan
 */
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
				//	System.out.println("preTimeout");
					Thread.sleep(timeout);
				//	System.out.println("postTimeout");
					
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