package com.komaxx.komaxx_gl.scenegraph.interaction;

import android.view.MotionEvent;

/**
 * Use this object to hold a history of MotionEvents
 * @author Matthias Schicker
 *
 */
public class MotionEventHistory  {
    private HistoryMotionEvent[] buffer;
    private int nextWriteIndex = 0;
    private int count = 0;
    
    public MotionEventHistory(int maxCapacity) {
        buffer = new HistoryMotionEvent[maxCapacity];
        for (int i = 0; i < buffer.length; i++) buffer[i] = new HistoryMotionEvent();
    }

    public void clear() {
        count = 0;
        nextWriteIndex = 0;
    }

    public void add(MotionEvent nuEntry) {
    	add(nuEntry.getEventTime(), nuEntry.getX(), nuEntry.getY());
    }

    public void add(long eventTime, float x, float y) {
    	HistoryMotionEvent toChange = buffer[nextWriteIndex]; 
        toChange.time = eventTime;
        toChange.x = x;
        toChange.y = y;
        
        nextWriteIndex = (nextWriteIndex + 1) % buffer.length;
        count++;
        if (count >= buffer.length) count = buffer.length;
	}

	public int size(){
        return (buffer == null) ? 0 : count;
    }

    public HistoryMotionEvent get(int i) {
        if (i >= count) return null;
        int retIndex = (nextWriteIndex - 1 + buffer.length - i) % buffer.length;
        return buffer[retIndex];
    }
    
    public class HistoryMotionEvent {
        public long time;
        public float x;
        public float y;
    }

	public void set(MotionEventHistory from) {
		System.arraycopy(from.buffer, 0, buffer, 0, buffer.length);
		nextWriteIndex = from.nextWriteIndex;
		count = from.count;
	}
}
