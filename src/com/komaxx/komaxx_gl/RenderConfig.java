package com.komaxx.komaxx_gl;

public class RenderConfig {
	public static boolean GL_DEBUG = false;
	public static boolean PROFILING = false;
	
	// TODO: Make this resolution dependent!
	public static float TAP_DISTANCE = 10;
	
	public static int TAP_MAX_TIME_MS = 250;

	/**
	 * When the view is idle, the sceneGraph will execute idleJobs until
	 * this amount of time was spent or the max number of jobs was reached
	 * Remaining jobs are rescheduled for the next frame.
	 */
	public static final int UI_IDLE_MAX_EXECUTION_TIME_MS = 150;
	
	/**
	 * Same as UI_IDLE_MAX_EXECUTION_TIME_MS but in the not-idle state.
	 */
	public static final int UI_NOT_IDLE_MAX_EXECUTION_TIME_MS = 5;
	
	/**
	 * When the view is idle, the sceneGraph will execute at most this amount
	 * of idleJobs or until the max amount of time was spent.
	 * Remaining jobs are rescheduled for the next frame.
	 */
	public static final int UI_IDLE_MAX_JOBS = 20;
	/**
	 * Same as UI_IDLE_MAX_JOBS but in the not-idle state.
	 */
	public static final int UI_NOT_IDLE_MAX_JOBS = 1;

	/**
	 * TODO Investigate whether this is actually helpful! May introduce
	 * memory issues!!
	 * 
	 * Set false to turn off all Bitmap.recycle calls. May help to solve a
	 * tombstoning problem...
	 */
	public static final boolean RECYCLE_BITMAPS = false;
}
