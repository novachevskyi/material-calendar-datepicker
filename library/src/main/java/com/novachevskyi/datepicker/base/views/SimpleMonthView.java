package com.novachevskyi.datepicker.base.views;

import android.content.Context;
import android.graphics.Canvas;

public class SimpleMonthView extends MonthView {

  public SimpleMonthView(Context context) {
    super(context);
  }

  @Override
  public void drawMonthDay(Canvas canvas, int year, int month, int day,
      int x, int y, int startX, int stopX, int startY, int stopY) {
    if (mSelectedDay == day) {
      canvas.drawCircle(x, y - (MINI_DAY_NUMBER_TEXT_SIZE / 3), DAY_SELECTED_CIRCLE_SIZE,
          mSelectedCirclePaint);
    }

    if (mHasToday && mToday == day) {
      mMonthNumPaint.setColor(mTodayNumberColor);
    } else {
      mMonthNumPaint.setColor(mDayTextColor);
    }
    canvas.drawText(String.format("%d", day), x, y, mMonthNumPaint);
  }
}
