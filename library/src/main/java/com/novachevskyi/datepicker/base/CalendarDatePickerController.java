package com.novachevskyi.datepicker.base;

import com.novachevskyi.datepicker.CalendarDatePickerDialog;
import com.novachevskyi.datepicker.base.adapters.MonthAdapter;

public interface CalendarDatePickerController {

  void onYearSelected(int year);

  void onDayOfMonthSelected(int year, int month, int day);

  void registerOnDateChangedListener(
      CalendarDatePickerDialog.OnDateChangedListener listener);

  void unregisterOnDateChangedListener(
      CalendarDatePickerDialog.OnDateChangedListener listener);

  MonthAdapter.CalendarDay getSelectedDay();

  int getFirstDayOfWeek();

  int getMinYear();

  int getMaxYear();
}
