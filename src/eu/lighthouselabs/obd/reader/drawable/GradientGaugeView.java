package eu.lighthouselabs.obd.reader.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public abstract class GradientGaugeView extends View {

	protected Context context = null;
	protected Paint paint = null;

	public GradientGaugeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		paint = new Paint();
	}

	@Override
	protected abstract void onDraw(Canvas canvas);

	protected void drawGradient(Canvas canvas, Drawable container, int offset, double value, double range, int angle) {
		int width = getWidth();
		int height = getHeight();
		int left = getLeft();
		int top = getTop();
		Log.i("width",String.format("%d %d",width,left));
		container.setBounds(left,top+offset,left+width,top+height+offset);
		container.draw(canvas);
		ShapeDrawable cover = new ShapeDrawable(new RectShape());
		double perc = value / range;
		if(angle ==0) {
			cover.setBounds(left+((int)(width * perc)), top+offset, left+width, top+height+offset);
		} else if (angle == 90) {
			cover.setBounds(left, top+offset, left+width, top+height-((int)(height * perc)));
		} else if (angle == 180) {
			cover.setBounds(left, top+offset, left+width-((int)(width * perc)), top+height+offset);
		} else if (angle == 270) {
			cover.setBounds(left, top+((int)(height * perc)), left+width, top+height+offset);
		}
		cover.draw(canvas);
	}
}
