package model;

import java.util.ArrayList;
import java.util.List;


public abstract class ThreadProcess {

	public static interface IProcessListener{
		/**
		 * Called after the process is completed and before sleeping.
		 */
		public void ProcessUpdated();
	}

	protected class ConditionnalThread extends Thread {
		protected boolean _threadRunning = false;
		private Runnable _loop;
		protected volatile Runnable _onExit = () -> {};

		protected ConditionnalThread(Runnable loop) {
			_loop = loop;
		}

		@Override
		public void run() {
			while(_threadRunning)
				_loop.run();

			_onExit.run();
		}		
	}

	private List<IProcessListener> _listeners = new ArrayList<>();

	protected FPSAnalyser _fpsAnalyser = new FPSAnalyser();

	protected ConditionnalThread _processLoopThread;


	public void StartThread()
	{
		_processLoopThread = new ConditionnalThread(() -> {
			Process();

			_fpsAnalyser.Tick();

			//If a call to Register or Unregister is done by a listener, _listeners wil be modified, so we cannot iterate over
			List<IProcessListener> listeners = new ArrayList<>(_listeners); 
			for(IProcessListener listener : listeners)
				listener.ProcessUpdated();

			try {
				Sleep();
			} catch (InterruptedException e) {/*Stop sleeping*/}
		});

		_processLoopThread.setDaemon(true);
		_processLoopThread._threadRunning = true;
		_processLoopThread.start();
	}

	public void StopThread()
	{
		_processLoopThread._threadRunning = false;
	}
	
	public void WakeUpThread() {
		_processLoopThread.interrupt();
	}

	public void Register(IProcessListener listener)
	{
		_listeners.add(listener);
	}

	public boolean Unregister(IProcessListener listener)
	{
		return _listeners.remove(listener);
	}

	public float GetProcessFPS()
	{
		return _fpsAnalyser.GetFPS();
	}

	protected abstract void Process();

	protected void Sleep() throws InterruptedException {}
}