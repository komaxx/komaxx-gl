package com.komaxx.komaxx_gl.texturing;


/**
 * Use this to create/obtain a TextureStrip.
 * 
 * @author Matthias Schicker
 */
public class TextureStripConfig extends TextureConfig {
	public static final byte PROPORTIONS_QUADRATIC = 0;
	public static final byte PROPORTIONS_COLUMN = 1;
	public static final byte PROPORTIONS_ROW = 2;
	
	public int segmentWidth = 128;
	public int segmentHeight = 128;
	
	public int minSegmentCount = 1;
	
	public byte proportionsType = PROPORTIONS_QUADRATIC;

	/**
	 * If true, segments may be rotated 90º to use the texture as efficient as possible
	 */
	public boolean mayRotate;
	

	@Override
	public TextureStripConfig clone(){
		TextureStripConfig ret = new TextureStripConfig();
		ret.mipMapped = this.mipMapped;
		ret.alphaChannel = this.alphaChannel;
		
		ret.segmentWidth = this.segmentWidth;
		ret.segmentHeight = this.segmentHeight;
		
		ret.minWidth = this.minWidth;
		ret.minHeight = this.minHeight;
		
		ret.minSegmentCount = this.minSegmentCount;
		
		ret.basicColor = this.basicColor;
		
		ret.nearestMapping = this.nearestMapping;
		ret.edgeBehavior = this.edgeBehavior;
		
		ret.proportionsType = this.proportionsType;
		
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (alphaChannel ? 1231 : 1237);
		result = prime * result + minSegmentCount;
		result = prime * result + (mipMapped ? 1231 : 1237);
		result = prime * result + segmentHeight;
		result = prime * result + segmentWidth;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		TextureStripConfig other = (TextureStripConfig) obj;
		if (alphaChannel != other.alphaChannel) return false;
		if (minSegmentCount != other.minSegmentCount) return false;
		if (nearestMapping != other.nearestMapping) return false;
		if (mipMapped != other.mipMapped) return false;
		if (segmentHeight != other.segmentHeight) return false;
		if (segmentWidth != other.segmentWidth) return false;
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(mipMapped ?  "Config: Mipmapped, " : "Config: Not mipmapped, ");
		ret.append(nearestMapping ?  "nearest mapping, " : "linear mapping, ");
		ret.append(alphaChannel ?  "alpha, " : "No alpha, ");
		ret.append(segmentWidth).append('x').append(segmentHeight).append(", ");
		ret.append(proportionsType==PROPORTIONS_QUADRATIC ?  "quadratic, " : (proportionsType==PROPORTIONS_COLUMN ? "column, " : "row, "));
		ret.append(">").append(minSegmentCount);
		return ret.toString();
	}
}
