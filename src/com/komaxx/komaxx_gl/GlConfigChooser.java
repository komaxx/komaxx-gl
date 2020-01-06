package com.komaxx.komaxx_gl;


import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView;

import com.komaxx.komaxx_gl.util.KoLog;
import com.komaxx.komaxx_gl.util.RenderUtil;

/**
 * This class essentially handles the GL
 * 
 * @author Matthias Schicker
 */
public class GlConfigChooser implements GLSurfaceView.EGLConfigChooser {
	private final GlConfig glConfig;
	
    public GlConfigChooser(GlConfig glConfig) {
		this.glConfig = glConfig;
	}

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        mValue = new int[1];

        int[] configSpec = null;
        int numConfigs = 0;
        
        if (glConfig.multisampling){
	        // Try to find a normal multisample configuration first.
	        configSpec = new int[]{
	                EGL10.EGL_RED_SIZE, glConfig.colorDepth.getR(),
	                EGL10.EGL_GREEN_SIZE, glConfig.colorDepth.getG(),
	                EGL10.EGL_BLUE_SIZE, glConfig.colorDepth.getB(),
	                EGL10.EGL_ALPHA_SIZE, glConfig.colorDepth.getA(),
	                EGL10.EGL_DEPTH_SIZE, glConfig.depthBufferBits.toValue(),
	                
	                
	                // Requires that setEGLContextClientVersion(2) is called on the view.
	                EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
	                EGL10.EGL_SAMPLE_BUFFERS, 1 /* true */,
	                EGL10.EGL_SAMPLES, 4,
	                EGL10.EGL_NONE
	        };
	
	
	        if (!egl.eglChooseConfig(display, configSpec, null, 0, mValue)) {
	            throw new IllegalArgumentException("eglChooseConfig failed");
	        }
	        numConfigs = mValue[0];
        }


        if (numConfigs <= 0) {
        	
        	if (glConfig.multisampling){
	            // No normal multisampling config was found. Try to create a
	            // converage multisampling configuration, for the nVidia Tegra2.
	            // See the EGL_NV_coverage_sample documentation.
	
	
	            final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
	            final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;
	
	
	            configSpec = new int[]{
	                    EGL10.EGL_RED_SIZE, glConfig.colorDepth.getR(),
	                    EGL10.EGL_GREEN_SIZE, glConfig.colorDepth.getG(),
	                    EGL10.EGL_BLUE_SIZE, glConfig.colorDepth.getB(),
	                    EGL10.EGL_ALPHA_SIZE, glConfig.colorDepth.getA(),
	                    EGL10.EGL_DEPTH_SIZE, glConfig.depthBufferBits.toValue(),
	                    
	                    EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
	                    EGL_COVERAGE_BUFFERS_NV, 1 /* true */,
	                    EGL_COVERAGE_SAMPLES_NV, 2,  // always 5 in practice on tegra 2
	                    EGL10.EGL_NONE
	            };
	
	
	            if (!egl.eglChooseConfig(display, configSpec, null, 0,
	                    mValue)) {
	                throw new IllegalArgumentException("2nd eglChooseConfig failed");
	            }
	            numConfigs = mValue[0];
        	}


            if (numConfigs <= 0) {
                // Give up, try without multisampling.
                configSpec = new int[]{
                        EGL10.EGL_RED_SIZE, glConfig.colorDepth.getR(),
                        EGL10.EGL_GREEN_SIZE, glConfig.colorDepth.getG(),
                        EGL10.EGL_BLUE_SIZE, glConfig.colorDepth.getB(),
                        EGL10.EGL_ALPHA_SIZE, glConfig.colorDepth.getA(),
                        EGL10.EGL_DEPTH_SIZE, glConfig.depthBufferBits.toValue(),
                        
                        EGL10.EGL_RENDERABLE_TYPE, 4 /* EGL_OPENGL_ES2_BIT */,
                        EGL10.EGL_NONE
                };


                if (!egl.eglChooseConfig(display, configSpec, null, 0,
                        mValue)) {
                    throw new IllegalArgumentException("3rd eglChooseConfig failed");
                }
                numConfigs = mValue[0];


                if (numConfigs <= 0) {
                  throw new IllegalArgumentException("No configs match configSpec");
                }
            } else {
                mUsesCoverageAa = true;
            }
        }


        // Get all matching configurations.
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs,
                mValue)) {
            throw new IllegalArgumentException("data eglChooseConfig failed");
        }


        // CAUTION! eglChooseConfigs returns configs with higher bit depth
        // first: Even though we asked for rgb565 configurations, rgb888
        // configurations are considered to be "better" and returned first.
        // You need to explicitly filter the data returned by eglChooseConfig!
        int index = -1;
        for (int i = 0; i < configs.length; ++i) {
            if (findConfigAttrib(egl, display, configs[i], EGL10.EGL_RED_SIZE, 0) == glConfig.colorDepth.getR()) {
                index = i;
                break;
            }
        }
        if (index == -1) {
        	KoLog.w(this, "Did not find sane config, using first");
        	index = 0;
        }
        EGLConfig config = configs.length > 0 ? configs[index] : null;
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        
        if (RenderConfig.GL_DEBUG) RenderUtil.checkGlError("choosing config");
        
        return config;
    }


    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }


    public boolean usesCoverageAa() {
        return mUsesCoverageAa;
    }


    private int[] mValue;
    private boolean mUsesCoverageAa;
}