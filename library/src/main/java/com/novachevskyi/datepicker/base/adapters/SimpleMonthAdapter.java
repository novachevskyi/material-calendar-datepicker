package com.novachevskyi.datepicker.base.adapters;

import android.content.Context;
import com.novachevskyi.datepicker.base.CalendarDatePickerController;
import com.novachevskyi.datepicker.base.views.MonthView;
import com.novachevskyi.datepicker.base.views.SimpleMonthView;

public class SimpleMonthAdapter extends MonthAdapter {
  public SimpleMonthAdapter(Context context, CalendarDatePickerController controller) {
    super(context, controller);
  }

  @Override
  public MonthView createMonthView(Context context) {
    return new SimpleMonthView(context);
  }
}
