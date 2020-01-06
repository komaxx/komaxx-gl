package com.komaxx.komaxx_gl.widgets;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.komaxx.komaxx_gl.RenderContext;
import com.komaxx.komaxx_gl.SceneGraphContext;
import com.komaxx.komaxx_gl.bound_meshes.BoundTexturedQuad;
import com.komaxx.komaxx_gl.scenegraph.interaction.ButtonInteractionInterpreter;
import com.komaxx.komaxx_gl.scenegraph.interaction.ButtonInteractionInterpreter.IButtonElement;
import com.komaxx.komaxx_gl.scenegraph.interaction.InteractionContext;
import com.komaxx.komaxx_gl.texturing.Texture;
import com.komaxx.komaxx_gl.util.InterpolatedValue;
import com.komaxx.komaxx_gl.util.KoLog;

public class GlStateButton extends BoundTexturedQuad implements IButtonElement{
	private final int checkedDrawableId;
	private final int uncheckedDrawableId;
	
	private ButtonInteractionInterpreter interactionInterpreter = 
		new ButtonInteractionInterpreter(this);

	private ICheckedChangedListener checkedChangedListener;
	
	private RectF uncheckedUvCoords;
	private RectF checkedUvCoords;

	private boolean checked = false;
	
	public GlStateButton(int uncheckedDrawableId, int checkedDrawableId){
		this.uncheckedDrawableId = uncheckedDrawableId;
		this.checkedDrawableId = checkedDrawableId;
		
		setAlphaAnimationDuration(InterpolatedValue.ANIMATION_DURATION_QUICK);
	}

	/**
	 * Draws the drawables of this button to the given texture. MUST be called
	 * for this button to be visible. </br>
	 * <b>Note</b> Expects the bitmap (bmp) to be set in the upper left corner of the
	 * texture.
	 */
	public int drawToTexture(Texture texture, Bitmap bmp, RenderContext rc, int xPos) {
		Canvas c = new Canvas(bmp);
		
		Rect tmpRect = new Rect();
		Drawable drawable = rc.resources.getDrawable(uncheckedDrawableId);
		tmpRect.set(xPos, 0, xPos + drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		sizeRectToFit(tmpRect, bmp);
		drawable.setBounds(tmpRect);
		drawable.draw(c);
		uncheckedUvCoords = texture.getUvCoords(tmpRect);
		xPos += tmpRect.width() + 1;
		
		drawable = rc.resources.getDrawable(checkedDrawableId);
		tmpRect.set(xPos, 0, xPos + drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		sizeRectToFit(tmpRect, bmp);
		drawable.setBounds(tmpRect);
		drawable.draw(c);
		checkedUvCoords = texture.getUvCoords(tmpRect);
		xPos += tmpRect.width() + 1;
		
		setTexCoordsUv(checked ? checkedUvCoords : uncheckedUvCoords);

		// adapt button to texture size
		positionXY(position.ulf[0], position.ulf[1],
				position.ulf[0] + tmpRect.width(), position.ulf[1] - tmpRect.height());
		
		return xPos;
	}

	private void sizeRectToFit(Rect tmpRect, Bitmap bmp) {
		if (tmpRect.height() > bmp.getHeight()){
			KoLog.w(this, "Insufficient texture space to draw texture. Fallback: shrinking.");
			// we need to rescale
			float sizeFactor = (float)bmp.getHeight() / (float)tmpRect.height();
			tmpRect.right = (int) (tmpRect.left + (float)tmpRect.width() * sizeFactor);
			tmpRect.bottom = (int) (tmpRect.top + (float)tmpRect.height() * sizeFactor);
		}
	}
	
	public boolean isChecked() {
		return checked;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;

		setTexCoordsUv(checked ? checkedUvCoords : uncheckedUvCoords);
	}
	
	public boolean onInteraction(InteractionContext ic) {
		return interactionInterpreter.onInteraction(ic);
	}

	@Override
	public boolean inBounds(float[] xy) {
		return contains(xy[0], xy[1]);
	}

	@Override
	public void down(InteractionContext ic) {
		setAlpha(1.5f);
	}

	@Override
	public void cancel(InteractionContext ic) {
		setAlpha(1);
	}

	@Override
	public void click(InteractionContext ic) {
		setAlphaDirect(1.5f);
		setAlpha(1);
		
		setChecked(!checked);
		
		notifyListener(ic);
	}
	
	public void setCheckedChangedListener(ICheckedChangedListener checkedChangedListener) {
		this.checkedChangedListener = checkedChangedListener;
	}
	
	private void notifyListener(InteractionContext ic) {
		ICheckedChangedListener checkedChangedListenerCopy = checkedChangedListener;
		if (checkedChangedListenerCopy != null){
			checkedChangedListenerCopy.onCheckedChanged(ic, this, checked);
		}	
	}

	public static interface ICheckedChangedListener {
		/**
		 * Called when the checked state changes. GL-thread.
		 */
		void onCheckedChanged(SceneGraphContext sc, GlStateButton button, boolean nowChecked);
	}
}
