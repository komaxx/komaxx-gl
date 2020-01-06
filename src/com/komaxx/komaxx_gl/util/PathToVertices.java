package com.komaxx.komaxx_gl.util;

import java.util.ArrayList;

import com.komaxx.komaxx_gl.math.Vector;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * This handy class takes an Android Path and samples (optimized)
 * into a series of vertices
 * 
 * @author Matthias Schicker
 */
public class PathToVertices {
	private static final float DEFAULT_SAMPLING_DISTANCE = 5f;
	/**
	 * If the dotProduct between two (normalized) tangents is bigger
	 * than this value, a refinement sample will be added.
	 */
	private static final float MIN_REFINEMENT_DELTA = 0.3f;
	
	/**
	 * If samples are closer than this times the sampling distance, 
	 * then no further refinement samples will be included. This 
	 * ensures that the interpreter will finish even when there are 
	 * sharp corners.
	 */
	private static final float MIN_REFINEMENT_DISTANCE_FACTOR = 0.1f;
	
	private PathToVertices(){}

	/**
	 * NOTE: Interprets only the first contour!
	 */
	public static float[][] interpretPath(Path p){
		return interpretPath(p, DEFAULT_SAMPLING_DISTANCE);
	}

	private static float[][] interpretPath(Path p, float samplingDistance) {
		PathMeasure pathMeasure = new PathMeasure(p, false);
		pathMeasure.nextContour();
		float length = pathMeasure.getLength();
		
		ArrayList<PathSample> samples = new ArrayList<PathToVertices.PathSample>();
		
		PathSample lastSample = new PathSample();
		lastSample.pathPos = 0;
		pathMeasure.getPosTan(0, lastSample.position, lastSample.tangent);
		samples.add(lastSample);
		
		for (float d = samplingDistance; d < length; d+=samplingDistance){
			PathSample nowSample = new PathSample();
			nowSample.pathPos = d;
			pathMeasure.getPosTan(d, nowSample.position, nowSample.tangent);
			
			if (!redundant(lastSample, nowSample)){
				samples.add(nowSample);
				lastSample = nowSample;
			}
		}
		
		PathSample finalSample = new PathSample();
		finalSample.pathPos = length;
		pathMeasure.getPosTan(length, finalSample.position, finalSample.tangent);
		samples.add(finalSample);
		
		return toArray(samples);
	}
	
	private static boolean redundant(PathSample lastSample, PathSample nowSample) {
		Vector.normalize2(lastSample.tangent);
		Vector.aToB2(tmpAtoB, lastSample.position, nowSample.position);
		Vector.normalize2(tmpAtoB);
		
		float dotProduct = Vector.dotProduct2(lastSample.tangent, tmpAtoB);
		
		return Math.abs(dotProduct) > 0.995f;
	}

	public static float[][] interpretPathOld(Path p, float samplingDistance) {
		ArrayList<PathSample> rawSamples = getRawSamples(p, samplingDistance);
		ArrayList<PathSample> shortList = getShortSamples(rawSamples);
		
		ArrayList<PathSample> refinedList = refineList(p, shortList, samplingDistance);
		
		return toArray(refinedList);
	}


	private static float[][] toArray(ArrayList<PathSample> refinedList) {
		int samples = refinedList.size();
		float[][] ret = new float[samples][];
		
		for (int i = 0; i < samples; i++){
			ret[i] = refinedList.get(i).position;
		}
		
		return ret;
	}

	private static ArrayList<PathSample> refineList(Path p, 
			ArrayList<PathSample> samples, float sampleDistance) {

		// TODO optimization point!
		// approach: compute delta of adjacent tangents. If below threshold, add sample.

		float minRefinementDistance = sampleDistance * MIN_REFINEMENT_DISTANCE_FACTOR;
		
		PathMeasure pathMeasure = new PathMeasure(p, false);
		
		PathSample lastSample = samples.get(0);
		for (int i = 1; i < samples.size(); i++){
			PathSample nowSample = samples.get(i);
			
			if (needsRefinement(lastSample, nowSample, minRefinementDistance)){
				PathSample refineSample = buildRefineSample(lastSample, nowSample, pathMeasure);
				samples.add(i+1, refineSample);
			}
			
			lastSample = nowSample;
		}
		
		return samples;
	}

	private static PathSample buildRefineSample(PathSample a, PathSample b, PathMeasure pathMeasure) {
		PathSample nuSample = new PathSample();
		float pathPos = (a.pathPos + b.pathPos) * 0.5f;
		pathMeasure.getPosTan(pathPos, nuSample.position, nuSample.tangent);
		return nuSample;
	}

	private static boolean needsRefinement(PathSample a, PathSample b, float minRefinementDistance) {
		if (Vector.distance2(a.position, b.position) < minRefinementDistance) return false;
		
		float[] aTan = a.tangent;
		if (!Vector.isNormalized2(aTan)) aTan = Vector.normalize2(aTan);
		
		float[] bTan = Vector.normalize2(b.tangent);
		if (!Vector.isNormalized2(bTan)) bTan = Vector.normalize2(bTan);
		
		float dotProduct = Vector.dotProduct2(aTan, bTan);
		
		return dotProduct < MIN_REFINEMENT_DELTA;
	}

	private static ArrayList<PathSample> getRawSamples(Path p, float samplingDistance) {
		PathMeasure pathMeasure = new PathMeasure(p, false);
		
		ArrayList<PathSample> samples = new ArrayList<PathSample>();
		
		pathMeasure.nextContour();

		float contourLength = pathMeasure.getLength();

		for (float d = 0; d < contourLength; d += samplingDistance){
			PathSample nowSample = new PathSample();
			pathMeasure.getPosTan(d, nowSample.position, nowSample.tangent);
			nowSample.pathPos = d;
			samples.add(nowSample);
		}

		// and add the closing sample
		PathSample nowSample = new PathSample();
		nowSample.pathPos = contourLength;
		pathMeasure.getPosTan(Float.MAX_VALUE, nowSample.position, nowSample.tangent);
		samples.add(nowSample);
		
		KoLog.i("FreeformRim", "Created set with "+samples.size()+" samples");
		
		return samples;
	}

	private static ArrayList<PathSample> getShortSamples(ArrayList<PathSample> rawSamples) {
		ArrayList<PathSample> shortList = new ArrayList<PathSample>();
		
		// now: combine samples to larger chunks (== better performance)
		if (rawSamples.size() > 2){
			PathSample beforeLastSample = rawSamples.get(rawSamples.size()-1);
			PathSample lastSample = rawSamples.get(rawSamples.size()-2);

			shortList.add(beforeLastSample);
			
			for (int i = rawSamples.size()-3; i >= 0; --i){
				PathSample nowSample = rawSamples.get(i);
				
				if (!sampleRedundant(lastSample, beforeLastSample, nowSample)){
					shortList.add(lastSample);
				}
				
				beforeLastSample = lastSample;
				lastSample = nowSample;
			}

			// ... and add the endpoint
			shortList.add(lastSample);
		} else {
			shortList.addAll(rawSamples);
		}

		KoLog.i("FreeformRim", "Created SHORT set with "+shortList.size()+" samples");

		return shortList;
	}
	
	private static final float[] tmpAtoB = new float[2];
	private static final float[] tmpNormal = new float[2];
	private static boolean sampleRedundant(PathSample toCheck, PathSample before, PathSample after) {
		// the same position?
		if (Vector.distance2(toCheck.position, before.position) < 0.000001f){
			return true;
		}
		
		// on the same tangent?
		Vector.aToB2(tmpAtoB, before.position, after.position);
		Vector.normal2(tmpNormal, tmpAtoB);
		
		Vector.aToB2(tmpAtoB, before.position, toCheck.position);
		
		float dotProduct = Vector.dotProduct2(tmpNormal, tmpAtoB);
		
		return Math.abs(dotProduct) < 0.000001f;
	}

	private static class PathSample {
		public float pathPos;
		public float[] position = new float[2];
		public float[] tangent = new float[2];
		
		@Override
		public String toString() {
			return Vector.toString(position, 3);
		}
	}


	// ///////////////////////////////////////////////////////
	// test
	public static void runTest(){
		long startTime = System.currentTimeMillis();
		
		Path testPath1 = new Path();
		testPath1.moveTo(0, 0);
		testPath1.lineTo(0, 100);
		testPath1.lineTo(100, 100);
		testPath1.lineTo(100, 0);
		testPath1.rCubicTo(0, 0, 50, 50, 100, 100);
		testPath1.rQuadTo(50, 50, 100, 100);
		
		interpretPath(testPath1);
		
		KoLog.i("TST", "path interpreted in " +(System.currentTimeMillis()-startTime)+ " ms");
	}
}
