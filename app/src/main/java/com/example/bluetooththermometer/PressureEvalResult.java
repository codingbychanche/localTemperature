package com.example.bluetooththermometer;

/**
 * A data model containing information about air pressure tendency
 */
public class PressureEvalResult {

    private int tendencySymbol;
    private float totalTimePressureIsRecorded;


    public PressureEvalResult(int tendencySymbol, float totalTimePressureIsRecorded) {
        this.tendencySymbol = tendencySymbol;
        this.totalTimePressureIsRecorded = totalTimePressureIsRecorded;
    }

    public int getTendencySymbol() {
        return tendencySymbol;
    }

    public void setTendencySymbol (int tendencySymbol) {
        this.tendencySymbol = tendencySymbol;
    }

    public float getTotalTimePressureIsRecorded() {
        return totalTimePressureIsRecorded;
    }

    public void setTotalTimePressureIsRecorded(float totalTimePressureIsRecorded) {
        this.totalTimePressureIsRecorded = totalTimePressureIsRecorded;
    }
}
