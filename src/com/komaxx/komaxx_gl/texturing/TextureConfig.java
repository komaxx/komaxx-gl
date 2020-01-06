package com.komaxx.komaxx_gl.texturing;

import android.opengl.GLES20;

/**
 * Use this to create/obtain a Texture.
 * 
 * @author Matthias Schicker
 */
public class TextureConfig {
	public static final int EDGE_CLAMP = GLES20.GL_CLAMP_TO_EDGE;
	public static final int EDGE_REPEAT = GLES20.GL_REPEAT;
	public static final int EDGE_MIRROR_REPEAT = GLES20.GL_MIRRORED_REPEAT;
	
	public boolean mipMapped = false;
	public boolean alphaChannel = false;
	
	/**
	 * When true, nearest neighbour texture mapping is used instead of linear mapping.
	 * Not compatible with mipmapping.
	 */
	public boolean nearestMapping = false;
	
	public int minWidth = 32;
	public int minHeight = 32;
	
	/**
	 * Only one of EDGE_xx values is allowed here
	 */
	public int edgeBehavior = EDGE_CLAMP;

	/**
	* The complete texture will be pre-filled with this color.
	*/
	public int basicColor = 0xFF000000;
	
	@Override
	public TextureConfig clone(){
		TextureConfig ret = new TextureConfig();
		ret.mipMapped = this.mipMapped;
		ret.alphaChannel = this.alphaChannel;
		
		ret.basicColor = this.basicColor;
		
		ret.nearestMapping = this.nearestMapping;
		ret.edgeBehavior = this.edgeBehavior;
		
		return ret;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (alphaChannel ? 1231 : 1237);
		result = prime * result + basicColor;
		result = prime * result + edgeBehavior;
		result = prime * result + minHeight;
		result = prime * result + minWidth;
		result = prime * result + (mipMapped ? 1231 : 1237);
		result = prime * result + (nearestMapping ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextureConfig other = (TextureConfig) obj;
		if (alphaChannel != other.alphaChannel)
			return false;
		if (basicColor != other.basicColor)
			return false;
		if (edgeBehavior != other.edgeBehavior)
			return false;
		if (minHeight != other.minHeight)
			return false;
		if (minWidth != other.minWidth)
			return false;
		if (mipMapped != other.mipMapped)
			return false;
		if (nearestMapping != other.nearestMapping)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append(mipMapped ?  "Config: Mipmapped, " : "Config: Not mipmapped, ");
		ret.append("min-size: " +minWidth+ "x" + minHeight);
		ret.append(nearestMapping ?  "nearest mapping, " : "linear mapping, ");
		ret.append(alphaChannel ?  "alpha, " : "No alpha, ");
		return ret.toString();
	}
}
