package com.novachevskyi.datepicker.base.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import com.novachevskyi.datepicker.CalendarDatePickerDialog;
import com.novachevskyi.datepicker.base.CalendarDatePickerController;
import com.novachevskyi.datepicker.base.adapters.MonthAdapter;
import com.novachevskyi.datepicker.base.adapters.SimpleMonthAdapter;
import com.novachevskyi.datepicker.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class DayPickerView extends ListView implements OnScrollListener,
    CalendarDatePickerDialog.OnDateChangedListener {

  private static final String TAG = "MonthFragment";

  protected static final int GOTO_SCROLL_DURATION = 250;
  protected static final int SCROLL_CHANGE_DELAY = 40;
  public static final int LIST_TOP_OFFSET = -1;

  private static final SimpleDateFormat YEAR_FORMAT =
      new SimpleDateFormat("yyyy", Locale.getDefault());

  protected float mFriction = 1.0f;

  protected Context mContext;
  protected Handler mHandler;

  protected MonthAdapter.CalendarDay mSelectedDay = new MonthAdapter.CalendarDay();
  protected MonthAdapter mAdapter;

  protected MonthAdapter.CalendarDay mTempDay = new MonthAdapter.CalendarDay();

  protected int mCurrentMonthDisplayed;
  protected long mPreviousScrollPosition;
  protected int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;
  protected int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

  private CalendarDatePickerController mController;
  private boolean mPerformingScroll;

  public DayPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public DayPickerView(Context context, CalendarDatePickerController controller) {
    super(context);
    init(context);
    setController(controller);
  }

  public void setController(CalendarDatePickerController controller) {
    mController = controller;
    mController.registerOnDateChangedListener(this);
    refreshAdapter();
    onDateChanged();
  }

  public void init(Context context) {
    mHandler = new Handler();
    setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    setDrawSelectorOnTop(false);

    mContext = context;
    setUpListView();
  }

  public void onChange() {
    refreshAdapter();
  }

  protected void refreshAdapter() {
    if (mAdapter == null) {
      mAdapter = createMonthAdapter(getContext(), mController);
    } else {
      mAdapter.setSelectedDay(mSelectedDay);
    }

    setAdapter(mAdapter);
  }

  public abstract MonthAdapter createMonthAdapter(Context context,
      CalendarDatePickerController controller);

  @SuppressLint("NewApi")
  protected void setUpListView() {
    setCacheColorHint(0);
    setDivider(null);
    setItemsCanFocus(true);
    setFastScrollEnabled(false);
    setVerticalScrollBarEnabled(false);
    setOnScrollListener(this);
    setFadingEdgeLength(0);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      setFriction(ViewConfiguration.getScrollFriction() * mFriction);
    }
  }

  public boolean goTo(MonthAdapter.CalendarDay day, boolean animate, boolean setSelected,
      boolean forceScroll) {
    if (setSelected) {
      mSelectedDay.set(day);
    }

    mTempDay.set(day);
    final int position = (day.year - mController.getMinYear())
        * SimpleMonthAdapter.MONTHS_IN_YEAR + day.month;

    View child;
    int i = 0;
    int top;

    do {
      child = getChildAt(i++);
      if (child == null) {
        break;
      }
      top = child.getTop();
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "child at " + (i - 1) + " has top " + top);
      }
    } while (top < 0);

    int selectedPosition;
    if (child != null) {
      selectedPosition = getPositionForView(child);
    } else {
      selectedPosition = 0;
    }

    if (setSelected) {
      mAdapter.setSelectedDay(mSelectedDay);
    }

    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "GoTo position " + position);
    }

    if (position != selectedPosition || forceScroll) {
      setMonthDisplayed(mTempDay);
      mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;
      if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        smoothScrollToPositionFromTop(
            position, LIST_TOP_OFFSET, GOTO_SCROLL_DURATION);
        return true;
      } else {
        postSetSelection(position);
      }
    } else if (setSelected) {
      setMonthDisplayed(mSelectedDay);
    }
    return false;
  }

  public void postSetSelection(final int position) {
    clearFocus();
    post(new Runnable() {

      @Override
      public void run() {
        setSelection(position);
      }
    });
    onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_IDLE);
  }

  @Override
  public void onScroll(
      AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    MonthView child = (MonthView) view.getChildAt(0);
    if (child == null) {
      return;
    }

    mPreviousScrollPosition =
        (long) (view.getFirstVisiblePosition() * child.getHeight() - child.getBottom());
    mPreviousScrollState = mCurrentScrollState;
  }

  protected void setMonthDisplayed(MonthAdapter.CalendarDay date) {
    mCurrentMonthDisplayed = date.month;
    invalidateViews();
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    mScrollStateChangedRunnable.doScrollStateChange(scrollState);
  }

  protected ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();

  protected class ScrollStateRunnable implements Runnable {

    private int mNewState;

    public void doScrollStateChange(int scrollState) {
      mHandler.removeCallbacks(this);
      mNewState = scrollState;
      mHandler.postDelayed(this, SCROLL_CHANGE_DELAY);
    }

    @Override
    public void run() {
      mCurrentScrollState = mNewState;
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG,
            "new scroll state: " + mNewState + " old state: " + mPreviousScrollState);
      }

      if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
          && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE
          && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
        mPreviousScrollState = mNewState;
        int i = 0;
        View child = getChildAt(i);
        while (child != null && child.getBottom() <= 0) {
          child = getChildAt(++i);
        }
        if (child == null) {
          return;
        }
        int firstPosition = getFirstVisiblePosition();
        int lastPosition = getLastVisiblePosition();
        boolean scroll = firstPosition != 0 && lastPosition != getCount() - 1;
        final int top = child.getTop();
        final int bottom = child.getBottom();
        final int midpoint = getHeight() / 2;
        if (scroll && top < LIST_TOP_OFFSET) {
          if (bottom > midpoint) {
            smoothScrollBy(top, GOTO_SCROLL_DURATION);
          } else {
            smoothScrollBy(bottom, GOTO_SCROLL_DURATION);
          }
        }
      } else {
        mPreviousScrollState = mNewState;
      }
    }
  }

  public int getMostVisiblePosition() {
    final int firstPosition = getFirstVisiblePosition();
    final int height = getHeight();

    int maxDisplayedHeight = 0;
    int mostVisibleIndex = 0;
    int i = 0;
    int bottom = 0;
    while (bottom < height) {
      View child = getChildAt(i);
      if (child == null) {
        break;
      }
      bottom = child.getBottom();
      int displayedHeight = Math.min(bottom, height) - Math.max(0, child.getTop());
      if (displayedHeight > maxDisplayedHeight) {
        mostVisibleIndex = i;
        maxDisplayedHeight = displayedHeight;
      }
      i++;
    }
    return firstPosition + mostVisibleIndex;
  }

  @Override
  public void onDateChanged() {
    goTo(mController.getSelectedDay(), false, true, true);
  }

  private MonthAdapter.CalendarDay findAccessibilityFocus() {
    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child instanceof MonthView) {
        final MonthAdapter.CalendarDay focus = ((MonthView) child).getAccessibilityFocus();
        if (focus != null) {
          if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((MonthView) child).clearAccessibilityFocus();
          }
          return focus;
        }
      }
    }

    return null;
  }

  private boolean restoreAccessibilityFocus(MonthAdapter.CalendarDay day) {
    if (day == null) {
      return false;
    }

    final int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child instanceof MonthView) {
        if (((MonthView) child).restoreAccessibilityFocus(day)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  protected void layoutChildren() {
    final MonthAdapter.CalendarDay focusedDay = findAccessibilityFocus();
    super.layoutChildren();
    if (mPerformingScroll) {
      mPerformingScroll = false;
    } else {
      restoreAccessibilityFocus(focusedDay);
    }
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setItemCount(-1);
  }

  private String getMonthAndYearString(MonthAdapter.CalendarDay day) {
    Calendar cal = Calendar.getInstance();
    cal.set(day.year, day.month, day.day);

    return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        + " "
        + YEAR_FORMAT.format(cal.getTime());
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
  }

  @SuppressLint("NewApi")
  @Override
  public boolean performAccessibilityAction(int action, Bundle arguments) {
    if (action != AccessibilityNodeInfo.ACTION_SCROLL_FORWARD &&
        action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
      return super.performAccessibilityAction(action, arguments);
    }

    int firstVisiblePosition = getFirstVisiblePosition();
    int month = firstVisiblePosition % 12;
    int year = firstVisiblePosition / 12 + mController.getMinYear();
    MonthAdapter.CalendarDay day = new MonthAdapter.CalendarDay(year, month, 1);

    if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
      day.month++;
      if (day.month == 12) {
        day.month = 0;
        day.year++;
      }
    } else {
      View firstVisibleView = getChildAt(0);

      if (firstVisibleView != null && firstVisibleView.getTop() >= -1) {
        day.month--;
        if (day.month == -1) {
          day.month = 11;
          day.year--;
        }
      }
    }

    Utils.tryAccessibilityAnnounce(this,
        getMonthAndYearString(day));
    goTo(day, true, false, true);
    mPerformingScroll = true;
    return true;
  }
}
