package com.komaxx.komaxx_gl.scenegraph.analysis;

import java.util.ArrayDeque;
import java.util.Deque;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.util.KoLog;


/**
 * Collects profiling data in the rendering process to find bottlenecks.
 * 
 * @author Matthias Schicker
 */
public class RenderProfiler implements IRenderProfiler  {
//	private Deque<ProfilerReport> profilerReports = new ArrayDeque<RenderProfiler.ProfilerReport>();
	private Deque<ProfilerReport> reportPool = new ArrayDeque<RenderProfiler.ProfilerReport>();
	
	private ProfilerReport currentReport;
	private ProfilerReport lastFrameReport;
	private ProfilerReport referenceReport;
	
	private long tmpTime = 0;
	
	public RenderProfiler(){
		referenceReport = getReport().reset();
	}
	
	@Override
	public void frameStart() {
		currentReport = getReport();
		currentReport.frameStartTime = System.nanoTime();
		if (lastFrameReport != null){
			currentReport.interFrameTime = currentReport.frameStartTime - lastFrameReport.frameEndTime;
		}
	}
	
	@Override
	public void frameDone(RenderContext rc) {
		currentReport.frameEndTime = System.nanoTime();
		
		cumulateCurrentToReferenceReport();
		
		long frameTime = currentReport.frameEndTime - currentReport.frameStartTime;
		float referenceDifference = 
			(float)(frameTime-referenceReport.frameEndTime) / (float)referenceReport.frameEndTime;
		referenceDifference = (int)(referenceDifference*1000f) / 10f;
		
		KoLog.i(this, "FrameTime: " 
				+  referenceDifference + "% , InterFrameTime: " + currentReport.interFrameTime
				+  ", global runnables time: " +currentReport.globalRunnablesTime);
		
		lastFrameReport = currentReport;
		currentReport = null;
	}

	private void cumulateCurrentToReferenceReport() {
		double REFERENCE_ADAPTION_SPEED = 0.98f;
		
		referenceReport.frameEndTime = 
			(long)(REFERENCE_ADAPTION_SPEED * referenceReport.frameEndTime) + 
			(long)(1.0- REFERENCE_ADAPTION_SPEED) * (currentReport.frameEndTime-currentReport.frameStartTime);
		
		referenceReport.interFrameTime = 
			(long)(REFERENCE_ADAPTION_SPEED * referenceReport.interFrameTime) + 
			(long)(1.0- REFERENCE_ADAPTION_SPEED) * (currentReport.interFrameTime);
	}

	private ProfilerReport getReport() {
		if (!reportPool.isEmpty()) return reportPool.pop();
		return new ProfilerReport();
	}

	@Override
	public void globalRunnablesStart() {
		tmpTime =  System.nanoTime();
	}
	
	@Override
	public void globalRunnablesDone() {
		currentReport.globalRunnablesTime = System.nanoTime() - tmpTime;
	}
	
	@Override
	public void startPath(Path path) {
		tmpTime = System.nanoTime();
	}

	@Override
	public void pathDone(Path path) {
		long pathTime = System.nanoTime() - tmpTime;
		path.updateRenderTime(pathTime);
	}
	
	private static class ProfilerReport {
		public long frameEndTime;
		public long interFrameTime;
		public long frameStartTime;
		public long globalRunnablesTime;
		
		public ProfilerReport reset() {
			globalRunnablesTime = 0;
			return this;
		}
	}
}
