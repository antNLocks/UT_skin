package model;

import java.util.ArrayList;
import java.util.List;


public abstract class ThreadProcess {

	public static interface IProcessListener{
		/**
		 * Called after the process is completed and before sleeping.
		 * @return Want to receive future updates.
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

			for(IProcessListener listener : _listeners)
				listener.ProcessUpdated();
			
			Sleep();
		});

		_processLoopThread.setDaemon(true);
		_processLoopThread._threadRunning = true;
		_processLoopThread.start();
	}

	public void StopThread()
	{
		_processLoopThread._threadRunning = false;
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

	protected void Sleep() {}
}
