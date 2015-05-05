package com.novachevskyi.datepicker.base.views;

import android.content.Context;
import android.util.AttributeSet;
import com.novachevskyi.datepicker.base.CalendarDatePickerController;
import com.novachevskyi.datepicker.base.adapters.MonthAdapter;
import com.novachevskyi.datepicker.base.adapters.SimpleMonthAdapter;

public class SimpleDayPickerView extends DayPickerView {

  public SimpleDayPickerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SimpleDayPickerView(Context context, CalendarDatePickerController controller) {
    super(context, controller);
  }

  @Override
  public MonthAdapter createMonthAdapter(Context context, CalendarDatePickerController controller) {
    return new SimpleMonthAdapter(context, controller);
  }
}
