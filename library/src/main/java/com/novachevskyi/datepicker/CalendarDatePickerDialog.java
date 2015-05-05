package com.novachevskyi.datepicker;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.novachevskyi.datepicker.base.CalendarDatePickerController;
import com.novachevskyi.datepicker.base.adapters.MonthAdapter;
import com.novachevskyi.datepicker.base.animators.AccessibleDateAnimator;
import com.novachevskyi.datepicker.base.views.DayPickerView;
import com.novachevskyi.datepicker.base.views.SimpleDayPickerView;
import com.novachevskyi.datepicker.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;

public class CalendarDatePickerDialog extends DialogFragment implements
    OnClickListener, CalendarDatePickerController {

  private static final String KEY_SELECTED_YEAR = "year";
  private static final String KEY_SELECTED_MONTH = "month";
  private static final String KEY_SELECTED_DAY = "day";
  private static final String KEY_LIST_POSITION = "list_position";
  private static final String KEY_WEEK_START = "week_start";
  private static final String KEY_YEAR_START = "year_start";
  private static final String KEY_YEAR_END = "year_end";

  private static final int DEFAULT_START_YEAR = 1900;
  private static final int DEFAULT_END_YEAR = 2100;

  private static final int ANIMATION_DURATION = 300;
  private static final int ANIMATION_DELAY = 500;

  private static final SimpleDateFormat DAY_FORMAT =
      new SimpleDateFormat("dd", Locale.getDefault());

  private final Calendar mCalendar = Calendar.getInstance();
  private OnDateSetListener mCallBack;
  private HashSet<OnDateChangedListener> mListeners = new HashSet<>();

  private AccessibleDateAnimator mAnimator;

  private TextView mDayOfWeekView;
  private LinearLayout mMonthAndDayView;
  private TextView mSelectedMonthTextView;
  private TextView mSelectedDayTextView;
  private DayPickerView mDayPickerView;

  private int mWeekStart = mCalendar.getFirstDayOfWeek();
  private int mMinYear = DEFAULT_START_YEAR;
  private int mMaxYear = DEFAULT_END_YEAR;

  private boolean mDelayAnimation = true;

  private String mDayPickerDescription;
  private String mSelectDay;

  public interface OnDateSetListener {
    void onDateSet(
        CalendarDatePickerDialog dialog,
        int year, int monthOfYear, int dayOfMonth);
  }

  public interface OnDateChangedListener {
    void onDateChanged();
  }

  public CalendarDatePickerDialog() {
  }

  public static CalendarDatePickerDialog newInstance(
      OnDateSetListener callBack, int year,
      int monthOfYear,
      int dayOfMonth) {
    CalendarDatePickerDialog
        ret = new CalendarDatePickerDialog();
    ret.initialize(callBack, year, monthOfYear, dayOfMonth);
    return ret;
  }

  public void initialize(OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
    mCallBack = callBack;
    mCalendar.set(Calendar.YEAR, year);
    mCalendar.set(Calendar.MONTH, monthOfYear);
    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Activity activity = getActivity();
    activity.getWindow().setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    if (savedInstanceState != null) {
      mCalendar.set(Calendar.YEAR, savedInstanceState.getInt(KEY_SELECTED_YEAR));
      mCalendar.set(Calendar.MONTH, savedInstanceState.getInt(KEY_SELECTED_MONTH));
      mCalendar.set(Calendar.DAY_OF_MONTH, savedInstanceState.getInt(KEY_SELECTED_DAY));
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(KEY_SELECTED_YEAR, mCalendar.get(Calendar.YEAR));
    outState.putInt(KEY_SELECTED_MONTH, mCalendar.get(Calendar.MONTH));
    outState.putInt(KEY_SELECTED_DAY, mCalendar.get(Calendar.DAY_OF_MONTH));
    outState.putInt(KEY_WEEK_START, mWeekStart);
    outState.putInt(KEY_YEAR_START, mMinYear);
    outState.putInt(KEY_YEAR_END, mMaxYear);
    int listPosition = mDayPickerView.getMostVisiblePosition();

    outState.putInt(KEY_LIST_POSITION, listPosition);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

    View view =
        inflater.inflate(R.layout.calendar_date_picker_dialog,
            null);

    mDayOfWeekView =
        (TextView) view.findViewById(R.id.date_picker_header);
    mMonthAndDayView = (LinearLayout) view.findViewById(R.id.date_picker_month_and_day);
    mMonthAndDayView.setOnClickListener(this);
    mSelectedMonthTextView =
        (TextView) view.findViewById(R.id.date_picker_month);
    mSelectedDayTextView =
        (TextView) view.findViewById(R.id.date_picker_day);

    int listPosition = -1;

    if (savedInstanceState != null) {
      mWeekStart = savedInstanceState.getInt(KEY_WEEK_START);
      mMinYear = savedInstanceState.getInt(KEY_YEAR_START);
      mMaxYear = savedInstanceState.getInt(KEY_YEAR_END);
      listPosition = savedInstanceState.getInt(KEY_LIST_POSITION);
    }

    final Activity activity = getActivity();
    mDayPickerView = new SimpleDayPickerView(activity, this);

    Resources res = getResources();
    mDayPickerDescription =
        res.getString(R.string.day_picker_description);
    mSelectDay = res.getString(R.string.select_day);

    mAnimator =
        (AccessibleDateAnimator) view.findViewById(R.id.animator);
    mAnimator.addView(mDayPickerView);
    mAnimator.setDateMillis(mCalendar.getTimeInMillis());

    Animation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(ANIMATION_DURATION);
    mAnimator.setInAnimation(animation);

    Animation animation2 = new AlphaAnimation(1.0f, 0.0f);
    animation2.setDuration(ANIMATION_DURATION);
    mAnimator.setOutAnimation(animation2);

    Button mDoneButton = (Button) view.findViewById(R.id.done);
    mDoneButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (mCallBack != null) {
          mCallBack.onDateSet(
              CalendarDatePickerDialog.this,
              mCalendar.get(Calendar.YEAR),
              mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        }
        dismiss();
      }
    });

    updateDisplay(false);
    setCurrentView();

    if (listPosition != -1) {
      mDayPickerView.postSetSelection(listPosition);
    }

    return view;
  }

  private void setCurrentView() {
    long millis = mCalendar.getTimeInMillis();

    ObjectAnimator pulseAnimator = Utils.getPulseAnimator(
        mMonthAndDayView, 0.9f,
        1.05f);
    if (mDelayAnimation) {
      pulseAnimator.setStartDelay(ANIMATION_DELAY);
      mDelayAnimation = false;
    }
    mDayPickerView.onDateChanged();
    mMonthAndDayView.setSelected(true);

    pulseAnimator.start();

    int flags = DateUtils.FORMAT_SHOW_DATE;
    String dayString = DateUtils.formatDateTime(getActivity(), millis, flags);
    mAnimator.setContentDescription(mDayPickerDescription + ": " + dayString);
    Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay);
  }

  private void updateDisplay(boolean announce) {
    if (mDayOfWeekView != null) {
      mDayOfWeekView.setText(mCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
          Locale.getDefault()).toUpperCase(Locale.getDefault()));
    }

    mSelectedMonthTextView.setText(mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
        Locale.getDefault()).toUpperCase(Locale.getDefault()));
    mSelectedDayTextView.setText(DAY_FORMAT.format(mCalendar.getTime()));

    long millis = mCalendar.getTimeInMillis();
    mAnimator.setDateMillis(millis);
    int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
    String monthAndDayText = DateUtils.formatDateTime(getActivity(), millis, flags);
    mMonthAndDayView.setContentDescription(monthAndDayText);

    if (announce) {
      flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
      String fullDateText = DateUtils.formatDateTime(getActivity(), millis, flags);
      Utils.tryAccessibilityAnnounce(mAnimator, fullDateText);
    }
  }

  public void setFirstDayOfWeek(int startOfWeek) {
    if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
      throw new IllegalArgumentException("Value must be between Calendar.SUNDAY and " +
          "Calendar.SATURDAY");
    }
    mWeekStart = startOfWeek;
    if (mDayPickerView != null) {
      mDayPickerView.onChange();
    }
  }

  public void setYearRange(int startYear, int endYear) {
    if (endYear <= startYear) {
      throw new IllegalArgumentException("Year end must be larger than year start");
    }
    mMinYear = startYear;
    mMaxYear = endYear;
    if (mDayPickerView != null) {
      mDayPickerView.onChange();
    }
  }

  public void setOnDateSetListener(OnDateSetListener listener) {
    mCallBack = listener;
  }

  private void adjustDayInMonthIfNeeded(int month, int year) {
    int day = mCalendar.get(Calendar.DAY_OF_MONTH);
    int daysInMonth = Utils.getDaysInMonth(month, year);
    if (day > daysInMonth) {
      mCalendar.set(Calendar.DAY_OF_MONTH, daysInMonth);
    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.date_picker_month_and_day) {
      setCurrentView();
    }
  }

  @Override
  public void onYearSelected(int year) {
    adjustDayInMonthIfNeeded(mCalendar.get(Calendar.MONTH), year);
    mCalendar.set(Calendar.YEAR, year);
    updatePickers();
    setCurrentView();
    updateDisplay(true);
  }

  @Override
  public void onDayOfMonthSelected(int year, int month, int day) {
    mCalendar.set(Calendar.YEAR, year);
    mCalendar.set(Calendar.MONTH, month);
    mCalendar.set(Calendar.DAY_OF_MONTH, day);
    updatePickers();
    updateDisplay(true);
  }

  private void updatePickers() {
    for (OnDateChangedListener mListener : mListeners) {
      mListener.onDateChanged();
    }
  }

  @Override
  public MonthAdapter.CalendarDay getSelectedDay() {
    return new MonthAdapter.CalendarDay(mCalendar);
  }

  @Override
  public int getMinYear() {
    return mMinYear;
  }

  @Override
  public int getMaxYear() {
    return mMaxYear;
  }

  @Override
  public int getFirstDayOfWeek() {
    return mWeekStart;
  }

  @Override
  public void registerOnDateChangedListener(OnDateChangedListener listener) {
    mListeners.add(listener);
  }

  @Override
  public void unregisterOnDateChangedListener(OnDateChangedListener listener) {
    mListeners.remove(listener);
  }
}
