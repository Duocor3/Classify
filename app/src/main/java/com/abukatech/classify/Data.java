package com.abukatech.classify;

import java.io.File;

public class Data {
    private String name;
    private File filePath;
    private String timeTaken;
    private int amOrPm;

    Data(String name, File filePath, String timeTaken, int amOrPm) {

        this.name = name;
        this.filePath = filePath;
        this.timeTaken = timeTaken;
        this.amOrPm = amOrPm;
    }

    public String getName() {
        return name;
    }

    public File getFilePath() {
        return filePath;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public int getAmOrPm() {
        return amOrPm;
    }

    public void setAmOrPm(int amOrPm) {
        this.amOrPm = amOrPm;
    }
}
