package com.novachevskyi.datepicker.base.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import com.novachevskyi.datepicker.base.CalendarDatePickerController;
import com.novachevskyi.datepicker.base.views.MonthView;
import java.util.Calendar;
import java.util.HashMap;

public abstract class MonthAdapter extends BaseAdapter implements MonthView.OnDayClickListener {

  private final Context mContext;
  private final CalendarDatePickerController mController;

  private CalendarDay mSelectedDay;

  public static final int MONTHS_IN_YEAR = 12;

  public static class CalendarDay {

    private Calendar calendar;
    public int year;
    public int month;
    public int day;

    public CalendarDay() {
      setTime(System.currentTimeMillis());
    }

    public CalendarDay(long timeInMillis) {
      setTime(timeInMillis);
    }

    public CalendarDay(Calendar calendar) {
      year = calendar.get(Calendar.YEAR);
      month = calendar.get(Calendar.MONTH);
      day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public CalendarDay(int year, int month, int day) {
      setDay(year, month, day);
    }

    public void set(CalendarDay date) {
      year = date.year;
      month = date.month;
      day = date.day;
    }

    public void setDay(int year, int month, int day) {
      this.year = year;
      this.month = month;
      this.day = day;
    }

    private void setTime(long timeInMillis) {
      if (calendar == null) {
        calendar = Calendar.getInstance();
      }
      calendar.setTimeInMillis(timeInMillis);
      month = calendar.get(Calendar.MONTH);
      year = calendar.get(Calendar.YEAR);
      day = calendar.get(Calendar.DAY_OF_MONTH);
    }
  }

  public MonthAdapter(Context context, CalendarDatePickerController controller) {
    mContext = context;
    mController = controller;
    init();
    setSelectedDay(mController.getSelectedDay());
  }

  public void setSelectedDay(CalendarDay day) {
    mSelectedDay = day;
    notifyDataSetChanged();
  }

  protected void init() {
    mSelectedDay = new CalendarDay(System.currentTimeMillis());
  }

  @Override
  public int getCount() {
    return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR;
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unchecked")
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    MonthView v;
    HashMap<String, Integer> drawingParams = null;
    if (convertView != null) {
      v = (MonthView) convertView;
      drawingParams = (HashMap<String, Integer>) v.getTag();
    } else {
      v = createMonthView(mContext);
      LayoutParams params = new LayoutParams(
          LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      v.setLayoutParams(params);
      v.setClickable(true);
      v.setOnDayClickListener(this);
    }
    if (drawingParams == null) {
      drawingParams = new HashMap<>();
    }
    drawingParams.clear();

    final int month = position % MONTHS_IN_YEAR;
    final int year = position / MONTHS_IN_YEAR + mController.getMinYear();

    int selectedDay = -1;
    if (isSelectedDayInMonth(year, month)) {
      selectedDay = mSelectedDay.day;
    }

    v.reuse();

    drawingParams.put(MonthView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
    drawingParams.put(MonthView.VIEW_PARAMS_YEAR, year);
    drawingParams.put(MonthView.VIEW_PARAMS_MONTH, month);
    drawingParams.put(MonthView.VIEW_PARAMS_WEEK_START, mController.getFirstDayOfWeek());
    v.setMonthParams(drawingParams);
    v.invalidate();
    return v;
  }

  public abstract MonthView createMonthView(Context context);

  private boolean isSelectedDayInMonth(int year, int month) {
    return mSelectedDay.year == year && mSelectedDay.month == month;
  }

  @Override
  public void onDayClick(MonthView view, CalendarDay day) {
    if (day != null) {
      onDayTapped(day);
    }
  }

  protected void onDayTapped(CalendarDay day) {
    mController.onDayOfMonthSelected(day.year, day.month, day.day);
    setSelectedDay(day);
  }
}
