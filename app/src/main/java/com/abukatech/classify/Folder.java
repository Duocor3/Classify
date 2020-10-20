package com.abukatech.classify;

import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class Folder {
    private File folder;
    private String name;//abcdefghijklmnopqrstuvwxyz
    private ArrayList<Calendar> periodStart;
    private ArrayList<Calendar> periodEnd;
    private Button buttonReference;
    private Calendar calendar;


    public Folder(File folder, String name, ArrayList<Calendar> periodStart,
                  ArrayList<Calendar> periodEnd, Button buttonReference) {
        this.folder = folder;
        this.name = name;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.buttonReference = buttonReference;

        calendar = Calendar.getInstance();
    }

    public String periodStartString() {
        return periodStart.get(Calendar.HOUR) + ":" + periodStart.get(Calendar.MINUTE);
    }

    public String periodEndString() {
        return periodEnd.get(Calendar.HOUR) + ":" + periodEnd.get(Calendar.MINUTE);
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getPeriodStart(int day) {
        return periodStart.get(day);
    }

    public ArrayList<Calendar> getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(ArrayList<Calendar> periodStart) {
        this.periodStart = periodStart;
    }

    public void setPeriodStart(int day, Calendar periodStart) {
        this.periodStart.set(day, periodStart);
    }

    public Calendar getPeriodEnd(int day) {
        return periodEnd.get(day);
    }

    public ArrayList<Calendar> getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(ArrayList<Calendar> periodEnd) {
        this.periodEnd = periodEnd;
    }

    public void setPeriodEnd(int day, Calendar periodEnd) {
        this.periodEnd.set(day, periodEnd);
    }

    public Button getButtonReference() {
        return buttonReference;
    }

    public void setButtonReference(Button buttonReference) {
        this.buttonReference = buttonReference;
    }
}
