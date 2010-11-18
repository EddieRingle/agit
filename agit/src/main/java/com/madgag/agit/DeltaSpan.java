package com.madgag.agit;

import static java.lang.Math.round;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class DeltaSpan extends MetricAffectingSpan {

	private final static int insertColour = 0x0000ff00,	deleteColour = 0x00ff0000;

	private final float magnitude;
	private final int alpha;
	private final boolean insertNotDelete;

	public DeltaSpan(boolean insertNotDelete, float progress) {
		this.insertNotDelete = insertNotDelete;
		this.magnitude = insertNotDelete ? progress : (1 - progress);

		alpha = round(magnitude * 0xff);
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public void updateDrawState(TextPaint textPaint) {
		textPaint.setTextSize(textPaint.getTextSize() * magnitude);
		textPaint.setAlpha(alpha);
		textPaint.bgColor = (insertNotDelete ? insertColour : deleteColour)	+ (alpha << 24);
	}

	@Override
	public void updateMeasureState(TextPaint ds) {
		ds.setTextSize(ds.getTextSize() * magnitude);
	}
}
