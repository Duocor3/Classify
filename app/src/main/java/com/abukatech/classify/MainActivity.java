package com.abukatech.classify;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.os.LocaleListCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Gallery.OnFragmentInteractionListener,
        Fullscreen.OnFragmentInteractionListener, Settings.OnFragmentInteractionListener {

    static final int REQUEST_TAKE_PHOTO = 1;
    static boolean temp = false;
    public static File file;
    public String prevLocation;

    private static List<Data> data;
    private List<Folder> allFolders;
    private static List<ArrayList<Calendar>> schedule;
    private static String whichPeriod = null;
    private boolean isCameraRunning = false;
    private boolean isAutoSorting = true;
    private boolean isBackupEnabled = false;
    private static ImageView fullScreen;
    private boolean isEditing = false;
    private boolean moveImage = false;

    private Data currentImage;
    private File photoFile;
    private String imageFileName;
    private TransitionDrawable clickedAnimation;

    private int daySelected;

    private static LinearLayout body;

    private Handler handler;

    private File currentPhotoPath;

    private Button period1, period2, period3, period4, period5, period6, period7, defaultFolder;

    private ImageButton settings, fab, edit;

    private TextView instructions;

    boolean permissionGranted = true;

    private static SharedPreferences prefs = null;

    private Intent intent;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fullScreen = findViewById(R.id.fullScreen);
        body = findViewById(R.id.body);

        // initializes the arraylist that holds references to the folder classes
        allFolders = new ArrayList<>();
        schedule = new ArrayList<>();

        // properly select which day when the app is created
        // fixed a very confusing bug
        if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2 > 4 ||
                Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2 < 0) {
            daySelected = 4;
        } else {
            daySelected = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
        }

        // sets button references to the variables
        period1 = findViewById(R.id.period1);
        period2 = findViewById(R.id.period2);
        period3 = findViewById(R.id.period3);
        period4 = findViewById(R.id.period4);
        period5 = findViewById(R.id.period5);
        period6 = findViewById(R.id.period6);
        period7 = findViewById(R.id.period7);
        // sets a button id for the "edit" button
        edit = findViewById(R.id.edit);
        // sets a button id for the default folder
        defaultFolder = findViewById(R.id.defaultFolder);
        // sets a textview id for the instructions
        instructions = findViewById(R.id.instructions);
        // sets a button id for the "settings" button
        settings = findViewById(R.id.settings);

        // makes folders if app is being run for the first time
        Folder folder;
        File documentsFolder;

        // sets up the handler
        handler = new Handler();

        // shared preferences to check if the app is being run for the first time
        prefs = getSharedPreferences("com.abukatech.classify", MODE_PRIVATE);

        intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // if app is run for first time, set toggle notifs to true
        if (prefs.getBoolean("firstrun", true)) {
            // makes the default times for the schedules
            // could probably make more efficient, but it's better than my previous one xD
            addTimeCalendar(7, 40, 0, 7, 40, 0, 7, 40, 0);
            addTimeCalendar(8, 41,0, 8, 41, 0, 8, 41, 0);
            addTimeCalendar(9, 33,0, 10, 30, 0, 8, 41, 0);
            addTimeCalendar(10,25,0, 10, 30, 0, 10, 30, 0);
            addTimeCalendar(11,17,0, 12, 55, 0, 10, 30 ,0);
            addTimeCalendar(12,30,0, 12, 55, 0, 12, 55, 0);
            addTimeCalendar(1,43,1, 2, 30, 1, 12, 55, 0);
            //calls the "home" notification right when school ends (over here), dont need it atm
            addTimeCalendar(2,30,1, 2, 30, 1, 11, 30, 1);
            addTimeCalendar(3, 0, 1, 3, 0, 1, 3, 0, 1);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notifs", true);

            // saves the schedule to sharedpreferences
            Gson gson = new Gson();
            String json = gson.toJson(schedule);
            editor.putString("schedule", json);

            editor.apply();
        } else {
            // loads the schedule from sharedpreferences if it isn't the first time the app is being run
            schedule = loadList("schedule");
            updateToCurrentDay(schedule);
        }

        file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/");

        // sort the files in the directory by creation date if it's not the first time the app was opened
        List<File> filesInDirectory = null;
        if (file.listFiles() != null) {
            filesInDirectory = Arrays.asList(file.listFiles());
            Collections.sort(filesInDirectory, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    return file.getName().compareTo(t1.getName());
                }
            });
        }

        // sets up the references to the folders present in the app storage
        boolean isThereFolders = file.listFiles() == null;
        for (int i = 1; i <= 7; i++) {
            // makes the default folders if there aren't any already (period 1-7)
            if (isThereFolders) {
                documentsFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/"+(i-1)+"Period " + i);
                documentsFolder.mkdirs();
            } else {
                // if the app already has folders set up, then just use those instead of making new ones
                documentsFolder = filesInDirectory.get(i-1);
            }

            // assigns each of the folders to the folder class
            folder = new Folder(documentsFolder, documentsFolder.getName(), schedule.get(i - 1), schedule.get(i), null);
            allFolders.add(folder);
        }

        // adds the button references to the folder class
        allFolders.get(0).setButtonReference(period1);
        allFolders.get(1).setButtonReference(period2);
        allFolders.get(2).setButtonReference(period3);
        allFolders.get(3).setButtonReference(period4);
        allFolders.get(4).setButtonReference(period5);
        allFolders.get(5).setButtonReference(period6);
        allFolders.get(6).setButtonReference(period7);

        // resets the name of the buttons to whatever the names of the folders in the app storage are
        for (Folder f : allFolders) {
            f.getButtonReference().setText(f.getName().substring(1));
        }

        // initializes data
        data = new ArrayList<>();

        // sets an event listener for the floating button (for taking a new photo)
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/Temp Folder");
                file.mkdirs();
                dispatchTakePictureIntent();
            }
        });

        // sets an event listener for the "edit" button
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEditing = !isEditing;
                moveImage = false;

                // if the app is in editing mode, the pencil shows up. Otherwise, the icon is removed
                if (isEditing) {
                    changeButtonIcons(false);
                    instructions.setText("Choose a folder to rename");
                } else {
                    // removes pencil icon from the button
                    if (moveImage) {
                        changeButtonIcons(false);
                        instructions.setText("Next, choose where to save the image");
                    } else {
                        changeButtonIcons(true);
                        instructions.setText("Take pictures of your school assignments");
                    }
                }
            }
        });

        // opens the settings fragment when you clicke the settings button
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });

        // sets event listeners for all the buttons
        period1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(0).getFolder().getName(), 0);
            }
        });

        period1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period1, false);
                showDialog("Edit", 1, allFolders.get(0));
                // buttonup animation
                buttonTransition(period1, true);
                return true;
            }
        });

        period2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(1).getFolder().getName(), 1);
            }
        });

        period2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period2, false);
                showDialog("Edit", 1, allFolders.get(1));
                // buttonup animation
                buttonTransition(period2, true);
                return true;
            }
        });

        period3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(2).getFolder().getName(), 2);
            }
        });

        period3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period3, false);
                showDialog("Edit", 1, allFolders.get(2));
                // buttonup animation
                buttonTransition(period3, true);
                return true;
            }
        });

        period4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(3).getFolder().getName(), 3);
            }
        });

        period4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period4, false);
                showDialog("Edit", 1, allFolders.get(3));
                // buttonup animation
                buttonTransition(period4, true);
                return true;
            }
        });

        period5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(4).getFolder().getName(), 4);
            }
        });

        period5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period5, false);
                showDialog("Edit", 1, allFolders.get(4));
                // buttonup animation
                buttonTransition(period5, true);
                return true;
            }
        });

        period6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(5).getFolder().getName(), 5);
            }
        });

        period6.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period6, false);
                showDialog("Edit", 1, allFolders.get(5));
                // buttonup animation
                buttonTransition(period6, true);
                return true;
            }
        });

        period7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed(allFolders.get(6).getFolder().getName(), 6);
            }
        });

        period7.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // buttondown animation
                buttonTransition(period7, false);
                showDialog("Edit", 1, allFolders.get(6));
                // buttonup animation
                buttonTransition(period7, true);
                return true;
            }
        });

        defaultFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folderButtonPressed("Temp Folder", defaultFolder);
            }
        });
    }

    @Override
    protected void onResume() {
        // only sets a notification if it's the first time the app was opened
        if (prefs.getBoolean("firstrun", true)) {
            // sets the alarm notification
            setAlarm();
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        // only clears if the camera isn't running (b/c onresume gets triggered by switching to camera)
        if (isCameraRunning) {
            // backs up the photo to the gallery if backup to gallery has been enabled
            if (isBackupEnabled) {
                backupData();
            }

            if (!isAutoSorting) {
                // makes it so when you click the folder after you take the picture, it moves the image to that folder
                moveImage = true;
                isEditing = false;
                // changes the icon of the button
                changeButtonIcons(false);
                instructions.setText("Next, choose where to save the image");
            }

            isCameraRunning = false;
        }

        if (temp && !isEditing) {
            data = fill_with_data();
        }

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            // pulls up the information page
            openGallery();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch(requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    isBackupEnabled = false;
                    toast(MainActivity.this, "Can't backup photos without permission", Toast.LENGTH_SHORT);
                }

                return;
            }
        }
    }

    // updates the schedule's day to the current day so that autosorting works
    private void updateToCurrentDay(List<ArrayList<Calendar>> arr) {
        // loops through and updates the day to the current day for each arraylist in the list
        for (int i = 0; i < arr.size(); i++) {
            for (int j = 0; j < arr.get(i).size(); j++) {
                arr.get(i).get(j).set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                arr.get(i).get(j).set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
                arr.get(i).get(j).set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
            }
        }
    }

    // sets up and modifies the alarm
    private void setAlarm() {
        // sets an alarm for when you get home
        long time = schedule.get(8).get(0).getTimeInMillis();

        // if the app is installed after when the notification is supposed to send, it changes the start day by 24 hrs
        if (System.currentTimeMillis() > time) {
            time += 86400000;
        }

        // cancels the alarm
        alarmManager.cancel(pendingIntent);
        // sets the alarm only if toggle notifs is true
        if (prefs.getBoolean("notifs", true)) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time,
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    // converts the schedule string back into an arraylist
    // copied from stack overflow
    private List<ArrayList<Calendar>> loadList(String yourKey) {
        Gson gson = new Gson();
        String json = prefs.getString(yourKey, null);

        Type type = new TypeToken< List<ArrayList<Calendar>> >() {}.getType();

        return gson.fromJson(json, type);
    }

    // sets up the button click animation for all the buttons
    private void buttonTransition(Button button, boolean reverse) {
        clickedAnimation = (TransitionDrawable) button.getBackground();

        if (!reverse) {
            clickedAnimation.startTransition(300);
        } else {
            clickedAnimation.reverseTransition(300);
        }
    }

    // makes a copy of the file in any directory
    private void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1+"");
        }
        catch (Exception e) {
            Log.e("tag", e+"");
        }
    }

    // backs up all the images by adding them to the gallery
    public void backupData() {
        // if permission is granted, then make a copy of the photo just taken and put it in the gallery
        if (permissionGranted) {
            File DCIM = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Classify");
            DCIM.mkdirs();

            copyFile(file.getPath(), "/" + photoFile.getName(), DCIM.getPath());
        }
    }

    // a simpler way to add values to the schedule arraylist
    public void addTimeCalendar(int reghr, int regmin, int regampm,
                                    int wedhr, int wedmin, int wedampm,
                                    int thuhr, int thumin, int thuampm) {

        // makes an arraylist with all the same values to put into schedule
        schedule.add(sameTimeArrayList(setDay(reghr, regmin, regampm)));
        // sets custom schedule for wendsday
        schedule.get(schedule.size()-1).set(2, setDay(wedhr, wedmin, wedampm));
        // sets custom schedule for thursday
        schedule.get(schedule.size()-1).set(3, setDay(thuhr, thumin, thuampm));
    }

    // sets the custom days if it's the first time running the app
    public Calendar setDay(int hour, int minute, int ampm) {
        Calendar calendar = Calendar.getInstance();

        // sets the values for the date on the calendar
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.AM_PM, ampm);

        return calendar;
    }

    // converts the calendar information to the am/pm format
    public String formatCalendar(Calendar calendar) {
        String formattedTime;
        int hour;

        if (calendar.get(Calendar.HOUR) == 0) {
            hour = 12;
        } else {
            hour = calendar.get(Calendar.HOUR);
        }

        // creates the string using the format method and a ternary operator
        formattedTime = String.format(Locale.US, "%2d:%02d %s", hour, calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM");

        return formattedTime;
    }

    // runs when you click any of the folder buttons
    public void folderButtonPressed(String whichButtonPressed, int whichFolder) {
        // buttondown animation
        buttonTransition(allFolders.get(whichFolder).getButtonReference(), false);
        if (!isEditing) {
            // if it's not in editing mode, but it was right after a picture was taken,
            // clicking the button moves the image to that folder
            if (moveImage) {
                // resets the normality of the buttons
                changeButtonIcons(true);
                chooseFolder(allFolders.get(whichFolder).getFolder());
            } else {
                whichPeriod = whichButtonPressed;
                data = fill_with_data();
                openGallery(data);
            }
        } else {
            // shows the dialog box to edit the name of the button and file
            showDialog("Edit", 1, allFolders.get(whichFolder));
        }
        // buttonup animation
        buttonTransition(allFolders.get(whichFolder).getButtonReference(), true);
    }

    // an overriden method for the default folder
    public void folderButtonPressed(String whichButtonPressed, Button button) {
        // buttondown animation
        buttonTransition(button, false);
        // opens the default folder gallery
        whichPeriod = whichButtonPressed;
        data = fill_with_data();
        openGallery(data);
        // buttonup animation
        buttonTransition(button, true);
    }

    // overidden method for the info screen
    public void openGallery() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_from_top,
                R.anim.enter_from_bottom, R.anim.exit_from_top);

        Gallery gallery = Gallery.newInstance();
        transaction.add(R.id.fragment_container, gallery, "Info").addToBackStack(null).commit();
    }

    // opens the gallery as a fragment
    public void openGallery(List<Data> data) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_from_top,
                R.anim.enter_from_bottom, R.anim.exit_from_top);

        Gallery gallery = Gallery.newInstance(data);
        transaction.add(R.id.fragment_container, gallery, "Gallery").addToBackStack(null).commit();
    }

    // opens the settings page as a fragment
    public void openSettings() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_from_top,
                R.anim.enter_from_bottom, R.anim.exit_from_top);

        Settings settings = Settings.newInstance(isAutoSorting, isBackupEnabled, prefs.getBoolean("notifs", true));
        transaction.add(R.id.fragment_container, settings, "Settings").addToBackStack(null).commit();
    }

    // moves the file to the folder you click on
    public void chooseFolder(File whichFolder) {
        // moves the file to wherever you clicked
        moveFile(prevLocation, whichFolder.toString() + "/" + imageFileName + ".jpg");
        instructions.setText("Image was saved to " + whichFolder.getName().substring(1));
        // removes the icon from the button
        changeButtonIcons(true);
        moveImage = false;
        isEditing = false;
    }

    // goes through all the buttons and changes their icon for different actions
    public void changeButtonIcons(boolean isNormal) {
        int background;

        // if isNormal is true, it changes the background of the buttons to their defaults
        if (isNormal) {
            background = R.drawable.clicked;
        } else {
            background = R.drawable.modifyclicked;
        }

        // loops through all the folders to get their button references
        for (Folder folder : allFolders) {
            // changes the xml resource for their backgrounds
            folder.getButtonReference().setBackgroundResource(background);
        }
    }

    // makes a toast pop up (function made so you can use within event listeners)
    public void toast(Context c, String message, int length) {
        Toast.makeText(c, message, length).show();
    }

    // changes background colors of the days of the week buttons
    public void daysOfWeekBackground(Button change, Button b1, Button b2, Button b3, Button b4, int daySelected) {
        change.setBackgroundResource(R.drawable.button);
        // resets the rest of the buttons
        b1.setBackgroundResource(R.drawable.modifybutton);
        b2.setBackgroundResource(R.drawable.modifybutton);
        b3.setBackgroundResource(R.drawable.modifybutton);
        b4.setBackgroundResource(R.drawable.modifybutton);

        // saves which day was selected
        this.daySelected = daySelected;
    }

    // creates a dialog box
    public void showDialog(String title, final int useCase, final Folder whichFolder) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertToShow;

        // makes a custom title (all it is is centered)
        TextView customTitle = new TextView(this);
        customTitle.setText(title);
        customTitle.setPadding(16, 30, 16, 16);
        customTitle.setGravity(Gravity.CENTER);
        customTitle.setTextSize(20);
        customTitle.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        builder.setCustomTitle(customTitle);

        builder.setTitle(title);

        // Set up what's inside the dialog box
        View dialogView = View.inflate(this, R.layout.dialog_box, null);
        // initializes variables
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2;
        final EditText input = dialogView.findViewById(R.id.editName);
        final Button startTime = dialogView.findViewById(R.id.startTime);
        final Button endTime = dialogView.findViewById(R.id.endTime);
        final Switch customSchedule = dialogView.findViewById(R.id.customSchedule);
        final LinearLayout daysOfTheWeek = dialogView.findViewById(R.id.daysOfTheWeek);

        final Button mon = dialogView.findViewById(R.id.mon);
        final Button tue = dialogView.findViewById(R.id.tue);
        final Button wed = dialogView.findViewById(R.id.wed);
        final Button thu = dialogView.findViewById(R.id.thu);
        final Button fri = dialogView.findViewById(R.id.fri);

        // sets the times inside the box to the time range for that day if its a weekday
        if (day >= 0 && day < 4) {
            startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(day)));
            endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(day)));
            daySelected = day;
        } else {
            // if it's a weekend, it  uses daySelected to set the text in the box
            startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
            endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
        }

        // makes days of the week invisible when the dialog box first pops up
        // will need to replace later with a way to save the values even after the dialog box is closed
        daysOfTheWeek.setVisibility(View.GONE);
        // event listener for the custom schedule toggle
        customSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only shows the weekday buttons if customization is enabled. Otherwise, it hides them
                if (customSchedule.isChecked()) {
                    daysOfTheWeek.setVisibility(View.VISIBLE);
                } else {
                    daysOfTheWeek.setVisibility(View.GONE);
                }
            }
        });

        // sets onclick listeners for the days of the week buttons
        mon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysOfWeekBackground(mon, tue, wed, thu, fri, 0);
                startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
                endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
            }
        });

        tue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysOfWeekBackground(tue, mon, wed, thu, fri, 1);
                startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
                endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
            }
        });

        wed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysOfWeekBackground(wed, tue, mon, thu, fri, 2);
                startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
                endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
            }
        });

        thu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysOfWeekBackground(thu, tue, wed, mon, fri, 3);
                startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
                endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
            }
        });

        fri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                daysOfWeekBackground(fri, tue, wed, thu, mon, 4);
                startTime.setText("Starts " + formatCalendar(whichFolder.getPeriodStart(daySelected)));
                endTime.setText("Ends " + formatCalendar(whichFolder.getPeriodEnd(daySelected)));
            }
        });

        // allows you to set the start time when the start button is clicked
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // displays a time picker dialog
                showTimePicker(whichFolder, startTime, "Starts ", customSchedule);
            }
        });

        // allows you to set the end time when the end button is clicked
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // displays a time picker dialog
                showTimePicker(whichFolder, endTime, "Ends ", customSchedule);
            }
        });

        // Specify the type of input expected
        builder.setView(dialogView);

        // Sets up the buttons after you press "ok" on the camera screen
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // stops invalid input
                if (input.getText().toString().matches("^.*[^a-zA-Z0-9 ].*$")) {
                    // gets the user input
                    whichPeriod = whichFolder.getButtonReference().getText().toString();
                    toast(MainActivity.this, "Only letters and numbers allowed!", Toast.LENGTH_LONG);
                } else {
                    whichPeriod = input.getText().toString();
                }

                if (whichFolder != null) {
                    file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/" + whichFolder.getName().charAt(0) + whichPeriod);
                } else {
                    // searches through all the folders in the folder arraylist and finds the one with a matching button reference name
                    for (Folder f : allFolders) {
                        if (f.getButtonReference().getText().toString().equals(whichPeriod)) {
                            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/"
                                    + f.getName().charAt(0) + whichPeriod);
                            break;
                        } else {
                            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/" + whichPeriod);
                        }
                    }
                }

                // buttons do different actions based on whether it is editing a file or not
                // 1 == is editing
                if (useCase == 1) {
                    //  only edits if the edit name box isn't blank
                    if (input.getText().toString().length() > 0) {
                        // gets a reference to the old name of the folder
                        File oldFolder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                "Assignments/" + whichFolder.getName());

                        // renames the corresponding file and buttons
                        oldFolder.renameTo(file);
                        whichFolder.setFolder(file);
                        whichFolder.setName(whichFolder.getName().charAt(0) + whichPeriod);
                        whichFolder.getButtonReference().setText(whichPeriod);
                        // this line was added to fix the bug where you autosort a pic,
                        // change the name of the folder, press back, and open the app again
                        // that bug was caused by whichPeriod not having the number value of the folder along with the name (ex: 3gym vs gym)
                        whichPeriod = whichFolder.getName();
                    }

                    isEditing = false;
                    changeButtonIcons(true);
                    instructions.setText("Take pictures of your school assignments");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (useCase == -1) { // set up catchers for the cancel button later

                } else {
                    whichPeriod = "Cancel";
                }

                dialog.cancel();
            }
        });

        // shows the dialog box
        alertToShow = builder.create();
        alertToShow.show();
    }

    // makes ArrayList with same time
    public ArrayList<Calendar> sameTimeArrayList(Calendar myCalender) {
        ArrayList<Calendar> same = new ArrayList<>();

        // adds ArrayList with all same times
        for (int i = 0; i < 5; i++) {
            same.add(myCalender);
        }

        return same;
    }

    // shows the time picker dialog
    // copied and pasted from Stack Overflow
    public Calendar showTimePicker(final Folder whichFolder, final Button whichButton, final String startOrEnd, final Switch customSchedule) {
        final Calendar myCalender = Calendar.getInstance();
        int hour = myCalender.get(Calendar.HOUR_OF_DAY);
        int minute = myCalender.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener myTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (view.isShown()) {

                    myCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalender.set(Calendar.MINUTE, minute);

                    if (startOrEnd.equals("Starts ")) {
                        // changes input based on the customSchedule switch
                        if (customSchedule.isChecked()) {
                            whichFolder.setPeriodStart(daySelected, myCalender);
                        } else {
                            whichFolder.setPeriodStart(sameTimeArrayList(myCalender));
                        }
                    } else {
                        // changes input based on the customSchedule switch
                        if (customSchedule.isChecked()) {
                            whichFolder.setPeriodEnd(daySelected, myCalender);
                        } else {
                            whichFolder.setPeriodEnd(sameTimeArrayList(myCalender));
                        }
                    }

                    updateSchedule(whichFolder);

                    whichButton.setText(startOrEnd + formatCalendar(myCalender));
                }
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                myTimeListener, hour, minute, false);
        timePickerDialog.setTitle("Set Time:");
        timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        timePickerDialog.show();

        return myCalender;
    }

    // updates the schedule arraylist
    public void updateSchedule(Folder whichFolder) {
        schedule.set(Integer.parseInt(whichFolder.getName().substring(0, 1)), whichFolder.getPeriodStart());
        schedule.set(Integer.parseInt(whichFolder.getName().substring(0, 1))+1, whichFolder.getPeriodEnd());

        // saves the updated schedule to sharedpreferences
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(schedule);
        editor.putString("schedule", json);

        editor.apply();
    }

    // sorts the image into the right folder based on the time the photo was taken
    public void autoSort(Data image, Calendar timeTaken) {
        boolean wasSorted = false;

        // loops through all the folders and compares the time the photo was taken to the time ranges for each period
        for (Folder folder : allFolders) {
            // checks to see if the time the photo was taken is within the time range of one of the folderrs
            if (compareTimes(timeTaken, folder.getPeriodStart(daySelected), folder.getPeriodEnd(daySelected))) {
                moveFile(prevLocation, folder.getFolder().toString() + "/" + image.getName() + ".jpg");
                instructions.setText("Image was sorted to " + folder.getButtonReference().getText());
                wasSorted = true;
                break;
            }
        }

        // checks if the sorting was successful
        if (!wasSorted) {
            // if the time the photo was taken doesn't match with any time ranges,
            // then you need to click a folder to move the image there
            moveImage = true;
            isEditing = false;
            changeButtonIcons(false);
            instructions.setText("Next, choose where to save the image");
        }
    }

    // checks if the time given is between the two time ranges given
    public boolean compareTimes(Calendar timeTaken, Calendar periodStart, Calendar periodEnd) {
        if (timeTaken != null) {
            return periodStart.before(timeTaken) && periodEnd.after(timeTaken);
        }

        return false;
    }

    public List<Data> fill_with_data() {

        // to fix the "inconsistency detected" bug
        data.clear();

        // loops through all the photos in each directory and adds them to the data array
        if (whichPeriod != null) {
            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Assignments/" + whichPeriod);
            // in case that folder doesn't exist
            file.mkdirs();

            File[] children = file.listFiles();
            Calendar dateMade;
            String getTimes;

            for (File child : children) {
                // does not add the file if it isn't an image or if the image is null/broken
                if ((child.getAbsolutePath().substring(child.getAbsolutePath().lastIndexOf(".")).equals(".jpeg") ||
                        child.getAbsolutePath().substring(child.getAbsolutePath().lastIndexOf(".")).equals(".jpg") ||
                        child.getAbsolutePath().substring(child.getAbsolutePath().lastIndexOf(".")).equals(".png") ||
                        child.getAbsolutePath().substring(child.getAbsolutePath().lastIndexOf(".")).equals(".gif")) &&
                                (child.length() > 0)) {

                    // converts the time each file was made to regular form
                    dateMade = new GregorianCalendar();
                    dateMade.setTime(new Date(child.lastModified()));
                    getTimes = dateMade.get(Calendar.HOUR) + ":" + dateMade.get(Calendar.MINUTE);
                    // sets all the values and adds them to the data arraylist
                    data.add(new Data(child.getName(), child.getAbsoluteFile(), getTimes, dateMade.get(Calendar.AM_PM)));
                }
            }
        }

        return data;
    }

    // this function looks for a suitable camera app and opens it up so you can take the photo
    private void dispatchTakePictureIntent() {
        isCameraRunning = true;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.abukatech.classify.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // allows you to use the camera image right after you press ok
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // sets up a data instance for the photo taken
            Calendar dateMade = new GregorianCalendar();
            dateMade.setTime(new Date(photoFile.lastModified()));
            String getTimes = dateMade.get(Calendar.HOUR) + ":" + dateMade.get(Calendar.MINUTE);

            currentImage = new Data(imageFileName, currentPhotoPath, getTimes, dateMade.get(Calendar.AM_PM));

            // checks if autosorting is toggled
            if (isAutoSorting) {
                // runs the autosort function
                autoSort(currentImage, dateMade);
            }
        }
    }

    // creates the image file and names it
    private File createImageFile() throws IOException {
        File image;

        temp = false;
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";

        image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                file      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsoluteFile();
        prevLocation = image.getAbsolutePath();

        return image;
    }

    // moves file - copied from stackoverflow
    public void moveFile(String path_source, String path_destination) {
        File file_Source = new File(path_source);
        File file_Destination = new File(path_destination);

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(file_Source).getChannel();
            destination = new FileOutputStream(file_Destination).getChannel();

            long count = 0;
            long size = source.size();
            while((count += destination.transferFrom(source, count, size-count))<size);
            file_Source.delete();
        } catch (Exception e){
            Log.e("tag", "ERROR: " + e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }

                if (destination != null) {
                    destination.close();
                }
            } catch (Exception e) {
                Log.e("tag", "ERROR: " + e);
            }
        }

        temp = true;
    }

    @Override
    public void onFragmentInteraction(String sendBackText) {
        // closes the current fragment
        onBackPressed();
    }

    @Override
    public void onFragmentInteraction(boolean isAutoSorting, boolean isBackupEnabled, boolean isNotifsEnabled) {
        this.isAutoSorting = isAutoSorting;
        this.isBackupEnabled = isBackupEnabled;

        if (isBackupEnabled) {
            // requests permission to write to the gallery
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        // sets the notifs value in sharedpreferences to whatever the user just decided
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifs", isNotifsEnabled).apply();

        // cancels the alarm
        alarmManager.cancel(pendingIntent);
        setAlarm();

        onBackPressed();
    }

    public static ImageView getFullScreen() {
        return fullScreen;
    }

    public static LinearLayout getBody() {
        return body;
    }

    public static List<Data> getData() {
        return data;
    }

    public static SharedPreferences getPrefs() {
        return prefs;
    }
}
