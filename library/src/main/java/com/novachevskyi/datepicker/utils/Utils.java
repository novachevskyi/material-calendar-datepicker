package com.novachevskyi.datepicker.utils;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.os.Build;
import android.text.format.Time;
import android.view.View;
import java.util.Calendar;

public class Utils {

  public static final int PULSE_ANIMATOR_DURATION = 544;

  public static boolean isJellybeanOrLater() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  }

  @SuppressLint("NewApi")
  public static void tryAccessibilityAnnounce(View view, CharSequence text) {
    if (isJellybeanOrLater() && view != null && text != null) {
      view.announceForAccessibility(text);
    }
  }

  public static int getDaysInMonth(int month, int year) {
    switch (month) {
      case Calendar.JANUARY:
      case Calendar.MARCH:
      case Calendar.MAY:
      case Calendar.JULY:
      case Calendar.AUGUST:
      case Calendar.OCTOBER:
      case Calendar.DECEMBER:
        return 31;
      case Calendar.APRIL:
      case Calendar.JUNE:
      case Calendar.SEPTEMBER:
      case Calendar.NOVEMBER:
        return 30;
      case Calendar.FEBRUARY:
        return (year % 4 == 0) ? 29 : 28;
      default:
        throw new IllegalArgumentException("Invalid Month");
    }
  }

  public static int getWeeksSinceEpochFromJulianDay(int julianDay, int firstDayOfWeek) {
    int diff = Time.THURSDAY - firstDayOfWeek;
    if (diff < 0) {
      diff += 7;
    }
    int refDay = Time.EPOCH_JULIAN_DAY - diff;
    return (julianDay - refDay) / 7;
  }

  public static ObjectAnimator getPulseAnimator(View labelToAnimate, float decreaseRatio,
      float increaseRatio) {
    Keyframe k0 = Keyframe.ofFloat(0f, 1f);
    Keyframe k1 = Keyframe.ofFloat(0.275f, decreaseRatio);
    Keyframe k2 = Keyframe.ofFloat(0.69f, increaseRatio);
    Keyframe k3 = Keyframe.ofFloat(1f, 1f);

    PropertyValuesHolder scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3);
    PropertyValuesHolder scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3);
    ObjectAnimator pulseAnimator =
        ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX,
            scaleY);
    pulseAnimator.setDuration(PULSE_ANIMATOR_DURATION);

    return pulseAnimator;
  }
}
