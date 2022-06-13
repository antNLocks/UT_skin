package model;

import java.util.ArrayList;
import java.util.List;


public abstract class ThreadProcess {

	public static interface IProcessListener{
		public void ProcessUpdated();
	}
	
	private List<IProcessListener> _listeners = new ArrayList<>();

	
	private Thread _thread;
	private boolean _threadRunning = false;
	
	private FPSAnalyser _fpsAnalyser = new FPSAnalyser();
	
	public void StartThread()
	{
		_thread = new Thread(() -> {
			while(_threadRunning) {
				Process();
				
				_fpsAnalyser.Tick();
				
				for(IProcessListener listener : _listeners) 
					listener.ProcessUpdated();
				
				Sleep();
			}
		});
		
		_thread.setDaemon(true);
		_threadRunning = true;
		_thread.start();
	}
	
	public void StopThread()
	{
		_threadRunning = false;
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
