package com.komaxx.komaxx_gl.util;

import android.util.Log;
import android.util.SparseArray;

public class KoLog {
    public static String LOG_TAG = "KO_LOG";
	
    public static void e(Object src, String message) {
        Log.e(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void w(Object src, String message) {
        Log.w(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void i(Object src, String message) {
    	Log.i(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void v(Object src, String message) {
    	Log.v(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void d(Object src, String message) {
        Log.d(LOG_TAG, getTaggedMessage(src, message));
    }

    // multi parameter logging
    
    public static void e(Object src, String... message) {
        Log.e(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void w(Object src, String... message) {
        Log.w(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void i(Object src, String... message) {
    	Log.i(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void v(Object src, String... message) {
    	Log.v(LOG_TAG, getTaggedMessage(src, message));
    }
    
    public static void d(Object src, String... message) {
        Log.d(LOG_TAG, getTaggedMessage(src, message));
    }

    
	private static String getTaggedMessage(Object src, String[] message) {
        StringBuilder sb = getStringBuilder(Thread.currentThread().getId());
        sb.append('[');
        
        if ((src instanceof String)) sb.append(src);
        else sb.append(src.getClass().getSimpleName());
        sb.append("] ");
        sb.append("(").append(Thread.currentThread().getName()).append(") ");
        for (String m : message) sb.append(m);
        return sb.toString();
	}
	
	private static SparseArray<StringBuilder> stringBuilders = new SparseArray<StringBuilder>(15);
	private static StringBuilder getStringBuilder(long id) {
		StringBuilder ret = stringBuilders.get((int) id);
		if (ret == null){
			ret = new StringBuilder();
			stringBuilders.put((int) id, ret);
		}
		ret.setLength(0);
		return ret;
	}

	public static String getTaggedMessage(Object src, String message) {
        StringBuilder sb = getStringBuilder(Thread.currentThread().getId());

        sb.append('[');
        if ((src instanceof String)) sb.append(src);
        else sb.append(src.getClass().getSimpleName());
        sb.append("] ");
        sb.append("(").append(Thread.currentThread().getName()).append(") ");
        sb.append(message);
        return sb.toString();
    }

	public void v(String message) {
		Log.v(LOG_TAG, message);
	}

	public void d(String message) {
		Log.d(LOG_TAG, message);
	}

	public void w(String message) {
		Log.w(LOG_TAG, message);
	}

	public void e(String message) {
		Log.e(LOG_TAG, message);
	}

}
