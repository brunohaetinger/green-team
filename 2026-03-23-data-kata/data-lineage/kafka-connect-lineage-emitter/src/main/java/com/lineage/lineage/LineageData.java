package com.lineage.lineage;

public class LineageData {
    public final String jobName;
    public final String input;
    public final String output;

    public LineageData(String jobName, String input, String output) {
        this.jobName = jobName;
        this.input = input;
        this.output = output;
    }

    @Override
    public String toString() {
        return jobName + " | " + input + " -> " + output;
    }
}
