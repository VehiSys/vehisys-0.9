package eu.lighthouselabs.obd.reader.drawable;

import eu.lighthouselabs.obd.reader.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class FuelGaugeView extends GradientGaugeView {

	public final static int min_fuel = 0;
	public final static int max_fuel = 255;
	public final static int TEXT_SIZE = 14;
	public final static int range = max_fuel - min_fuel;
	private int fuel = min_fuel;

	public FuelGaugeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		paint = new Paint();
		paint.setTextSize(TEXT_SIZE);
		Typeface bold = Typeface.defaultFromStyle(Typeface.BOLD);
		paint.setTypeface(bold);
		paint.setStrokeWidth(3);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	public void setFuel(int fuel) {
		this.fuel = fuel;
		if (this.fuel < min_fuel) {
			this.fuel = min_fuel;
		}
		if (this.fuel > max_fuel) {
			this.fuel = max_fuel;
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Resources res = context.getResources();
		Drawable container = (Drawable) res.getDrawable(R.drawable.fuel_gauge);
		int width = getWidth();
		int height = getHeight();
		int left = getLeft();
		int top = getTop();
		int bottom = getBottom();
		paint.setColor(Color.BLUE);
		canvas.drawText("C",left,top+TEXT_SIZE,paint);
		paint.setColor(Color.RED);
		canvas.drawText("H", left+width-TEXT_SIZE, top+TEXT_SIZE, paint);
		paint.setColor(Color.YELLOW);
		canvas.drawText("E",left-width+TEXT_SIZE+20,top+height-TEXT_SIZE,paint);
		paint.setColor(Color.GREEN);
		canvas.drawText("F", left-width+TEXT_SIZE+20,top+10, paint);
		drawGradient(canvas, container, 0, fuel-min_fuel,range, 90);
	}
}
