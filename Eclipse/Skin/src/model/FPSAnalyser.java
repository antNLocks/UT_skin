package model;

import java.util.Calendar;

public class FPSAnalyser {
	
	private float _fps = 0;
	private int _maxCount = 30;
	private int _count = 0;
	private long _lastT = -1;
	
	public void Tick() {
		if(_count++ == _maxCount) {
			if(_lastT != -1) 
				_fps = 1000.0f * _maxCount / (Calendar.getInstance().getTime().getTime() - _lastT);
			
			_lastT = Calendar.getInstance().getTime().getTime();
			_count = 0;
		}
	}

	private int _timeDisplayWait = 1000;
	private long _lastDisplayValue = -1;
	private float _fpsDisplay;
	
	public float GetFPS() {
		if(Calendar.getInstance().getTime().getTime() - _lastDisplayValue > _timeDisplayWait) {
			_fpsDisplay = _fps;
			_lastDisplayValue = Calendar.getInstance().getTime().getTime();
		}
			
			
		return _fpsDisplay;
	}
	
	
}
