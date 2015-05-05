package com.novachevskyi.simplecalendarpicker;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.novachevskyi.datepicker.CalendarDatePickerDialog;
import java.util.Calendar;

public class MainActivity extends FragmentActivity {

  private static final String DATE_PICKER_TAG = "DATE_PICKER_TAG";

  private TextView selectedDateTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button pickDateButton = (Button) findViewById(R.id.btn_pick_date);
    pickDateButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showDatePicker();
      }
    });

    selectedDateTextView = (TextView) findViewById(R.id.tv_date);
  }

  private void showDatePicker() {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int monthOfYear = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    final CalendarDatePickerDialog dialog =
        CalendarDatePickerDialog.newInstance(dateSetListener, year, monthOfYear, dayOfMonth);

    dialog.show(getSupportFragmentManager(), DATE_PICKER_TAG);
  }

  CalendarDatePickerDialog.OnDateSetListener dateSetListener =
      new CalendarDatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear,
            int dayOfMonth) {
          selectedDateTextView.setText(
              String.format(getString(R.string.selected_date_template), year, monthOfYear + 1,
                  dayOfMonth));
        }
      };
}
