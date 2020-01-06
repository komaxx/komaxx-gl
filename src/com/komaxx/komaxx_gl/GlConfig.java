package com.komaxx.komaxx_gl;


/**
 * Needs to be passed as parameter to the lib to define basic properties
 * (color resolution, multisampling, ...). Changes to objects of this kind
 * will not have any effect after passing it to the SceneGraph / BasicView
 * 
 * @author Matthias Schicker
 */
public class GlConfig {
	public static enum ColorDepth {
		Color8888, Color5650, Color4444;
		
		public byte getA(){
			if (this == Color8888) return 8;
			else if (this == Color4444) return 4;
			else return 0;
		}
		
		public byte getR(){
			return getB();
		}
		
		public byte getG(){
			if (this == Color8888) return 8;
			else if (this == Color4444) return 4;
			else return 6;
		}
		
		public byte getB(){
			if (this == Color8888) return 8;
			else if (this == Color4444) return 4;
			else return 5;
		}
	}
	
	public static enum DepthBufferBits {
		NO_DEPTH_BUFFER, DEPTH_8, DEPTH_16;
		
		/**
		 * Delivers the bit value
		 */
		public byte toValue(){
			if (this == DEPTH_8) return 8;
			else if (this == DEPTH_16) return 16;
			else return 0;
		}
	}
	
	// ///////////////////////////////////////////////////////////
	
	public boolean multisampling = false;
	
	/**
	 * Define your desired colorDepth in here. Please not that this color depth
	 * is not necessarily supported by the platform; in that case something as
	 * close as possible will be used.
	 */
	public ColorDepth colorDepth = ColorDepth.Color8888;
	
	public DepthBufferBits depthBufferBits = DepthBufferBits.DEPTH_8;
	
	public GlConfig(){
	}

	public GlConfig(ColorDepth colorDepth, DepthBufferBits depthBufferBits, boolean multisampling){
		this.colorDepth = colorDepth;
		this.depthBufferBits = depthBufferBits;
		this.multisampling = multisampling;
	}
}
