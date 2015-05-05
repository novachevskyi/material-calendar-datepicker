package com.novachevskyi.datepicker.base.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.novachevskyi.datepicker.R;
import com.novachevskyi.datepicker.base.adapters.MonthAdapter;
import com.novachevskyi.datepicker.utils.Utils;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public abstract class MonthView extends View {

  public static final String VIEW_PARAMS_HEIGHT = "height";
  public static final String VIEW_PARAMS_MONTH = "month";
  public static final String VIEW_PARAMS_YEAR = "year";
  public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
  public static final String VIEW_PARAMS_WEEK_START = "week_start";

  protected static int DEFAULT_HEIGHT = 32;
  protected static int MIN_HEIGHT = 10;
  protected static final int DEFAULT_SELECTED_DAY = -1;
  protected static final int DEFAULT_WEEK_START = Calendar.SUNDAY;
  protected static final int DEFAULT_NUM_DAYS = 7;
  protected static final int DEFAULT_NUM_ROWS = 6;
  protected static final int MAX_NUM_ROWS = 6;

  private static final int SELECTED_CIRCLE_ALPHA = 60;

  protected static int DAY_SEPARATOR_WIDTH = 1;
  protected static int MINI_DAY_NUMBER_TEXT_SIZE;
  protected static int MONTH_LABEL_TEXT_SIZE;
  protected static int MONTH_DAY_LABEL_TEXT_SIZE;
  protected static int MONTH_HEADER_SIZE;
  protected static int DAY_SELECTED_CIRCLE_SIZE;

  protected int mPadding = 0;

  protected Paint mMonthNumPaint;
  protected Paint mMonthTitlePaint;
  protected Paint mMonthTitleBGPaint;
  protected Paint mSelectedCirclePaint;
  protected Paint mMonthDayLabelPaint;

  private final Formatter mFormatter;
  private final StringBuilder mStringBuilder;

  protected int mMonth;

  protected int mYear;
  protected int mWidth;
  protected int mRowHeight = DEFAULT_HEIGHT;
  protected boolean mHasToday = false;
  protected int mSelectedDay = -1;
  protected int mToday = DEFAULT_SELECTED_DAY;
  protected int mWeekStart = DEFAULT_WEEK_START;
  protected int mNumDays = DEFAULT_NUM_DAYS;
  protected int mNumCells = mNumDays;

  private final Calendar mCalendar;
  private final Calendar mDayLabelCalendar;
  private final MonthViewTouchHelper mTouchHelper;

  private int mNumRows = DEFAULT_NUM_ROWS;

  private OnDayClickListener mOnDayClickListener;
  private boolean mLockAccessibilityDelegate;

  protected int mDayTextColor;
  protected int mTodayNumberColor;
  protected int mMonthTitleColor;
  protected int mMonthTitleBGColor;

  public MonthView(Context context) {
    super(context);

    Resources res = context.getResources();

    mDayLabelCalendar = Calendar.getInstance();
    mCalendar = Calendar.getInstance();

    mDayTextColor = res.getColor(R.color.date_picker_text_normal);
    mTodayNumberColor = res.getColor(R.color.bpBlue);
    mMonthTitleColor = res.getColor(R.color.bpWhite);
    mMonthTitleBGColor = res.getColor(R.color.circle_background);

    mStringBuilder = new StringBuilder(50);
    mFormatter = new Formatter(mStringBuilder, Locale.getDefault());

    MINI_DAY_NUMBER_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.day_number_size);
    MONTH_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.month_label_size);
    MONTH_DAY_LABEL_TEXT_SIZE = res.getDimensionPixelSize(R.dimen.month_day_label_text_size);
    MONTH_HEADER_SIZE = res.getDimensionPixelOffset(R.dimen.month_list_item_header_height);
    DAY_SELECTED_CIRCLE_SIZE = res
        .getDimensionPixelSize(R.dimen.day_number_select_circle_radius);

    mRowHeight = (res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height)
        - MONTH_HEADER_SIZE) / MAX_NUM_ROWS;

    mTouchHelper = new MonthViewTouchHelper(this);
    ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    mLockAccessibilityDelegate = true;

    initView();
  }

  @Override
  public void setAccessibilityDelegate(AccessibilityDelegate delegate) {
    if (!mLockAccessibilityDelegate
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      super.setAccessibilityDelegate(delegate);
    }
  }

  public void setOnDayClickListener(OnDayClickListener listener) {
    mOnDayClickListener = listener;
  }

  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_UP:
        final int day = getDayFromLocation(event.getX(), event.getY());
        if (day >= 0) {
          onDayClick(day);
        }
        break;
    }
    return true;
  }

  protected void initView() {
    mMonthTitlePaint = new Paint();
    mMonthTitlePaint.setFakeBoldText(true);
    mMonthTitlePaint.setAntiAlias(true);
    mMonthTitlePaint.setTextSize(MONTH_LABEL_TEXT_SIZE);
    mMonthTitlePaint.setColor(mDayTextColor);
    mMonthTitlePaint.setTextAlign(Align.CENTER);
    mMonthTitlePaint.setStyle(Style.FILL);

    mMonthTitleBGPaint = new Paint();
    mMonthTitleBGPaint.setFakeBoldText(true);
    mMonthTitleBGPaint.setAntiAlias(true);
    mMonthTitleBGPaint.setColor(mMonthTitleBGColor);
    mMonthTitleBGPaint.setTextAlign(Align.CENTER);
    mMonthTitleBGPaint.setStyle(Style.FILL);

    mSelectedCirclePaint = new Paint();
    mSelectedCirclePaint.setFakeBoldText(true);
    mSelectedCirclePaint.setAntiAlias(true);
    mSelectedCirclePaint.setColor(mTodayNumberColor);
    mSelectedCirclePaint.setTextAlign(Align.CENTER);
    mSelectedCirclePaint.setStyle(Style.FILL);
    mSelectedCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);

    mMonthDayLabelPaint = new Paint();
    mMonthDayLabelPaint.setAntiAlias(true);
    mMonthDayLabelPaint.setTextSize(MONTH_DAY_LABEL_TEXT_SIZE);
    mMonthDayLabelPaint.setColor(mDayTextColor);
    mMonthDayLabelPaint.setStyle(Style.FILL);
    mMonthDayLabelPaint.setTextAlign(Align.CENTER);
    mMonthDayLabelPaint.setFakeBoldText(true);

    mMonthNumPaint = new Paint();
    mMonthNumPaint.setAntiAlias(true);
    mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
    mMonthNumPaint.setStyle(Style.FILL);
    mMonthNumPaint.setTextAlign(Align.CENTER);
    mMonthNumPaint.setFakeBoldText(false);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    drawMonthTitle(canvas);
    drawMonthDayLabels(canvas);
    drawMonthNums(canvas);
  }

  private int mDayOfWeekStart = 0;

  public void setMonthParams(HashMap<String, Integer> params) {
    if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
      throw new InvalidParameterException("You must specify month and year for this view");
    }
    setTag(params);

    if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
      mRowHeight = params.get(VIEW_PARAMS_HEIGHT);
      if (mRowHeight < MIN_HEIGHT) {
        mRowHeight = MIN_HEIGHT;
      }
    }
    if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
      mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
    }

    mMonth = params.get(VIEW_PARAMS_MONTH);
    mYear = params.get(VIEW_PARAMS_YEAR);

    final Time today = new Time(Time.getCurrentTimezone());
    today.setToNow();
    mHasToday = false;
    mToday = -1;

    mCalendar.set(Calendar.MONTH, mMonth);
    mCalendar.set(Calendar.YEAR, mYear);
    mCalendar.set(Calendar.DAY_OF_MONTH, 1);
    mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

    if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
      mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
    } else {
      mWeekStart = mCalendar.getFirstDayOfWeek();
    }

    mNumCells = Utils.getDaysInMonth(mMonth, mYear);
    for (int i = 0; i < mNumCells; i++) {
      final int day = i + 1;
      if (sameDay(day, today)) {
        mHasToday = true;
        mToday = day;
      }
    }
    mNumRows = calculateNumRows();

    mTouchHelper.invalidateRoot();
  }

  public void reuse() {
    mNumRows = DEFAULT_NUM_ROWS;
    requestLayout();
  }

  private int calculateNumRows() {
    int offset = findDayOffset();
    int dividend = (offset + mNumCells) / mNumDays;
    int remainder = (offset + mNumCells) % mNumDays;
    return (dividend + (remainder > 0 ? 1 : 0));
  }

  private boolean sameDay(int day, Time today) {
    return mYear == today.year &&
        mMonth == today.month &&
        day == today.monthDay;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows
        + MONTH_HEADER_SIZE);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    mWidth = w;

    mTouchHelper.invalidateRoot();
  }

  private String getMonthAndYearString() {
    int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
        | DateUtils.FORMAT_NO_MONTH_DAY;
    mStringBuilder.setLength(0);
    long millis = mCalendar.getTimeInMillis();
    return DateUtils.formatDateRange(getContext(), mFormatter, millis, millis, flags,
        Time.getCurrentTimezone()).toString();
  }

  private void drawMonthTitle(Canvas canvas) {
    int x = (mWidth + 2 * mPadding) / 2;
    int y = (MONTH_HEADER_SIZE - MONTH_DAY_LABEL_TEXT_SIZE) / 2 + (MONTH_LABEL_TEXT_SIZE / 3);
    canvas.drawText(getMonthAndYearString(), x, y, mMonthTitlePaint);
  }

  private void drawMonthDayLabels(Canvas canvas) {
    int y = MONTH_HEADER_SIZE - (MONTH_DAY_LABEL_TEXT_SIZE / 2);
    int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);

    for (int i = 0; i < mNumDays; i++) {
      int calendarDay = (i + mWeekStart) % mNumDays;
      int x = (2 * i + 1) * dayWidthHalf + mPadding;
      mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
      canvas.drawText(mDayLabelCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT,
              Locale.getDefault()).toUpperCase(Locale.getDefault()), x, y,
          mMonthDayLabelPaint);
    }
  }

  protected void drawMonthNums(Canvas canvas) {
    int y = (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH)
        + MONTH_HEADER_SIZE;
    int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);
    int j = findDayOffset();
    for (int dayNumber = 1; dayNumber <= mNumCells; dayNumber++) {
      int x = (2 * j + 1) * dayWidthHalf + mPadding;

      int yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH;

      int startX = x - dayWidthHalf;
      int stopX = x + dayWidthHalf;
      int startY = y - yRelativeToDay;
      int stopY = startY + mRowHeight;

      drawMonthDay(canvas, mYear, mMonth, dayNumber, x, y, startX, stopX, startY, stopY);

      j++;
      if (j == mNumDays) {
        j = 0;
        y += mRowHeight;
      }
    }
  }

  public abstract void drawMonthDay(Canvas canvas, int year, int month, int day,
      int x, int y, int startX, int stopX, int startY, int stopY);

  private int findDayOffset() {
    return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
        - mWeekStart;
  }

  public int getDayFromLocation(float x, float y) {
    int dayStart = mPadding;
    if (x < dayStart || x > mWidth - mPadding) {
      return -1;
    }

    int row = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
    int column = (int) ((x - dayStart) * mNumDays / (mWidth - dayStart - mPadding));

    int day = column - findDayOffset() + 1;
    day += row * mNumDays;
    if (day < 1 || day > mNumCells) {
      return -1;
    }
    return day;
  }

  private void onDayClick(int day) {
    if (mOnDayClickListener != null) {
      mOnDayClickListener.onDayClick(this, new MonthAdapter.CalendarDay(mYear, mMonth, day));
    }

    mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED);
  }

  public MonthAdapter.CalendarDay getAccessibilityFocus() {
    final int day = mTouchHelper.getFocusedVirtualView();
    if (day >= 0) {
      return new MonthAdapter.CalendarDay(mYear, mMonth, day);
    }
    return null;
  }

  public void clearAccessibilityFocus() {
    mTouchHelper.clearFocusedVirtualView();
  }

  public boolean restoreAccessibilityFocus(MonthAdapter.CalendarDay day) {
    if ((day.year != mYear) || (day.month != mMonth) || (day.day > mNumCells)) {
      return false;
    }
    mTouchHelper.setFocusedVirtualView(day.day);
    return true;
  }

  private class MonthViewTouchHelper extends ExploreByTouchHelper {

    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private final Rect mTempRect = new Rect();
    private final Calendar mTempCalendar = Calendar.getInstance();

    public MonthViewTouchHelper(View host) {
      super(host);
    }

    public void setFocusedVirtualView(int virtualViewId) {
      getAccessibilityNodeProvider(
          MonthView.this).performAction(
          virtualViewId, AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS, null);
    }

    public void clearFocusedVirtualView() {
      final int focusedVirtualView = getFocusedVirtualView();
      if (focusedVirtualView != ExploreByTouchHelper.INVALID_ID) {
        getAccessibilityNodeProvider(
            MonthView.this).performAction(
            focusedVirtualView,
            AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
            null);
      }
    }

    @Override
    protected int getVirtualViewAt(float x, float y) {
      final int day = getDayFromLocation(x, y);
      if (day >= 0) {
        return day;
      }
      return ExploreByTouchHelper.INVALID_ID;
    }

    @Override
    protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
      for (int day = 1; day <= mNumCells; day++) {
        virtualViewIds.add(day);
      }
    }

    @Override
    protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
      event.setContentDescription(getItemDescription(virtualViewId));
    }

    @Override
    protected void onPopulateNodeForVirtualView(int virtualViewId,
        AccessibilityNodeInfoCompat node) {
      getItemBounds(virtualViewId, mTempRect);

      node.setContentDescription(getItemDescription(virtualViewId));
      node.setBoundsInParent(mTempRect);

      if (virtualViewId == mSelectedDay) {
        node.setSelected(true);
      }
    }

    @Override
    protected boolean onPerformActionForVirtualView(int virtualViewId, int action,
        Bundle arguments) {
      switch (action) {
        case AccessibilityNodeInfo.ACTION_CLICK:
          onDayClick(virtualViewId);
          return true;
      }

      return false;
    }

    private void getItemBounds(int day, Rect rect) {
      final int offsetX = mPadding;
      final int offsetY = MONTH_HEADER_SIZE;
      final int cellHeight = mRowHeight;
      final int cellWidth = ((mWidth - (2 * mPadding)) / mNumDays);
      final int index = ((day - 1) + findDayOffset());
      final int row = (index / mNumDays);
      final int column = (index % mNumDays);
      final int x = (offsetX + (column * cellWidth));
      final int y = (offsetY + (row * cellHeight));

      rect.set(x, y, (x + cellWidth), (y + cellHeight));
    }

    private CharSequence getItemDescription(int day) {
      mTempCalendar.set(mYear, mMonth, day);
      final CharSequence date = DateFormat.format(DATE_FORMAT,
          mTempCalendar.getTimeInMillis());

      if (day == mSelectedDay) {
        return getContext().getString(R.string.item_is_selected, date);
      }

      return date;
    }
  }

  public interface OnDayClickListener {
    void onDayClick(MonthView view,
        MonthAdapter.CalendarDay day);
  }
}
