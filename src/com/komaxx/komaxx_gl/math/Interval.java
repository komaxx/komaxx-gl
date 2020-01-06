package com.komaxx.komaxx_gl.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple class to store intervals of any kind, and to operate on them.
 * 
 * @author Matthias Schicker
 */
public class Interval {
	public float left;
	public float right;

	public Interval(){}
	
	public Interval(float left, float right){
		this.left = left;
		this.right = right;
	}

	public Interval(Interval i){
		this.left = i.left;
		this.right = i.right;
	}
	
	@Override
	public Interval clone(){
		return new Interval(left, right);
	}


	public void set(Interval ivl) {
		left = ivl.left;
		right = ivl.right;
	}
	
	public void set(float nuLeft, float nuRight){
		left = nuLeft;
		right = nuRight;
	}
	
	public boolean overlaps(Interval b){
		return (left <= b.right) && (right >= b.left);
	}
	
	public boolean overlaps(float left, float right){
		return this.left <= right && this.right >= left;
	}
	
	public float width() {
		return right-left;
	}
	
	/**
	 * Extends this interval to the bounds of the input interval. Careful, there
	 * is no check, whether these intervals actually overlap!
	 */
	public void merge(Interval toMergeWith){
		if (toMergeWith.left < this.left) this.left = toMergeWith.left;
		if (toMergeWith.right > this.right) this.right = toMergeWith.right;
	}
	
	/**
	 * Only true, when this interval completely overlaps the input interval
	 */
	public boolean covers(Interval in){
		return left<=in.left && right>=in.right;
	}
	
	@Override
	public String toString() {
		return "["+left+";"+right+"]";
	}
	
	/**
	 * Creates a new Array containing (new) intervals that span the complete
	 * covered area of the input interval. Sorted. If only none-overlapping intervals
	 * are given, this will result in a sorted copy of these intervals. <br>
	 * @param result		This list will be cleared and contain the result when finished.
	 * If <code>null</code>, a new list will be created.
	 * @param toCollapse	The intervals to be collapsed. <b>Will be sorted in here!</b>
	 * @return 	the input array, when provided. Otherwise a new list. 
	 */
	public static ArrayList<Interval> collapse(ArrayList<Interval> result, List<Interval> toCollapse){
		if (result == null) result = new ArrayList<Interval>();
		result.clear();
		
		int l = toCollapse.size();
		if (l < 1) return result;
		
		Collections.sort(toCollapse, startComparator);
		
		Interval currentCollector = new Interval(toCollapse.get(0));
		result.add(currentCollector);
		
		Interval nowInput;
		for (int i = 1; i < l; i++){
			nowInput = toCollapse.get(i);
			if (currentCollector.overlaps(nowInput)){
				currentCollector.merge(nowInput);
			} else {
				currentCollector = new Interval(nowInput);
				result.add(currentCollector);
			}
		}
		
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Interval)) return false;
		Interval b = (Interval)o;
		return left==b.left && right==b.right;
	}
	
	public void offset(float offset) {
		this.left += offset;
		this.right += offset;
	}

	@Override
	public int hashCode() {
		return Float.floatToIntBits(left) ^ Float.floatToIntBits(right);
	}
	
	/**
	 * Compares two non-overlapping and sorted interval lists (as delivered by <code>collapse</code>)
	 * and returns the changes. Thread safe if the parameters are not changed asynchronously.
	 */
	public static void diff(ArrayList<Interval> oldIntervals, ArrayList<Interval> nuIntervals, 
			ArrayList<Interval> resultRemovedIntervals, ArrayList<Interval> resultAddedIntervals) {
		
		resultRemovedIntervals.clear();
		resultAddedIntervals.clear();
		
		// approach: subtract all new intervals from old intervals, and the other way round.
		// TODO: This is actually not as efficient as possible ( O(n*m) instead of O(max(n,m) ).
		//		 Should do for now, but when this becomes a bottleneck -> optimize!
		
		for (Interval i : oldIntervals) resultRemovedIntervals.add(new Interval(i));
		removeAll(resultRemovedIntervals, nuIntervals);
		
		for (Interval i : nuIntervals) resultAddedIntervals.add(new Interval(i));
		removeAll(resultAddedIntervals, oldIntervals);
	}

	private static void removeAll(ArrayList<Interval> minuend, ArrayList<Interval> subtrahend) {
		Interval nowMinuend;
		for (int i = 0; i < minuend.size(); i++){
			nowMinuend = minuend.get(i);
			for (Interval nowSubtrahend : subtrahend){
				if (!nowMinuend.overlaps(nowSubtrahend)) continue;
				
				if (nowSubtrahend.covers(nowMinuend)){
					minuend.remove(i);
					--i;
					break;
				}
				
				if (nowMinuend.covers(nowSubtrahend)){
					minuend.add(i+1, new Interval(nowSubtrahend.right, nowMinuend.right));
					nowMinuend.right = nowSubtrahend.left;
					--i;
				} else {
					// overlaps on one side
					if (nowMinuend.left < nowSubtrahend.left) nowMinuend.right = nowSubtrahend.left;
					else nowMinuend.left = nowSubtrahend.right;
				}
			}
		}
	}

	private static final Comparator<Interval> startComparator = new Comparator<Interval>() {
		@Override
		public int compare(Interval a, Interval b) {
			float aLeft = a.left;
			float bLeft = b.left;
			return (aLeft < bLeft) ? -1 : ((aLeft > bLeft) ? 1 : 0); 
		}
	};

	/**
	 * Will move this interval inside of the bounds of the given
	 * other interval. Makes only sense (and works only then) when
	 * <i>this</i> is shorter than the parameter interval.
	 */
	public void moveInto(Interval i) {
		if (i.width() < this.width()) return;
		if (left < i.left) offset(i.left - left);
		if (right > i.right) offset(i.right - right);
	}
}
