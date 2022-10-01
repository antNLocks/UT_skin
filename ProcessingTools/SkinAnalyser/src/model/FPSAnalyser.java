package model;

import java.util.Calendar;

public class FPSAnalyser {
	
	private float _fps = 0;
	private int _count = 0;
	private long _lastT = -1;
	private long _timeCounting = 1500;
	
	public void Tick() {
		_count++;
		long dt = Calendar.getInstance().getTime().getTime() - _lastT;
		if(dt > _timeCounting) {
			if(_lastT != -1) 
				_fps = (1000.0f * _count) / dt;
			
			_lastT = Calendar.getInstance().getTime().getTime();
			_count = 0;
		}
	}

	
	public float GetFPS() {
		return _fps;
	}
}