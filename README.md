Simple Android Material Calendar DatePicker
======================================================

Simple Android Material Calendar DatePicker offers you the date picker as shown in [the Material Design spec](http://www.google.com/design/spec/components/pickers.html) but much simple, with an
easy API.
The library uses [the code from the Android frameworks](https://android.googlesource.com/platform/frameworks/opt/datetimepicker/) as a base and tweaked it to be as close as possible to Material Design example.

Support for Android 4.0 and newer.

<img src="https://drive.google.com/uc?export=download&id=0Bx_Utp9eUjBQbVJMbDJaTWVDUkU" />

Including in Your Project
=========================

Material Calendar DatePicker is presented as an [Android library project](http://developer.android.com/guide/developing/projects/projects-eclipse.html).

If you are a Gradle user you can also easily include the library:

```groovy
compile 'com.novachevskyi:material-calendar-datepicker:1.0.0'
```

If you are bringing in the support library you may need to add an exclusion:

```groovy
compile ("com.novachevskyi:material-calendar-datepicker:1.0.0") {
    exclude group: 'com.android.support', module: 'appcompat-v7'
}
```

Usage
=====

*For a working implementation of this project see the `app/` folder.*

Define the appropriate Handler callbacks:

```java
CalendarDatePickerDialog.OnDateSetListener dateSetListener =
      new CalendarDatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(CalendarDatePickerDialog dialog, int year, int monthOfYear,
            int dayOfMonth) {
            
        }
      };
```

Use Builder class to create a Dialog:

```java
Calendar calendar = Calendar.getInstance();
int year = calendar.get(Calendar.YEAR);
int monthOfYear = calendar.get(Calendar.MONTH);
int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

final CalendarDatePickerDialog dialog =
        CalendarDatePickerDialog.newInstance(dateSetListener, year, monthOfYear, dayOfMonth);

dialog.show(getSupportFragmentManager(), "DATE_PICKER_TAG");
```

License
=======

    Copyright 2015 Stanislav Novachevskyi

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
