package org.acastronovo.tesi;

/**
 *@author Cristian D'Ortona / Andrea Castronovo / Alberto Iantorni
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

import android.util.Log;

import java.util.concurrent.TimeUnit;

public class FitnessTracker {

    private static final String TAG = "FitnessTracker";

    //https://golf.procon.org/met-values-for-800-activities/ -> MET Values
    private static final double MET_WALKING = 3.5;
    private static final double MET_RUNNING = 6;
    private static final double MET_CYCLING = 7.5;

    public static final String WALKING = "walking";
    public static final String RUNNING = "running";
    public static final String CYCLING = "cycling";

    private static double chosenMET = 0.0d;

    private static long start;

    static void calculateCalories(String activityType){
        Log.d(TAG, "selected activity type:" + activityType);
        switch (activityType.toLowerCase()){
            case WALKING:
                chosenMET = MET_WALKING;
                startCounting();
                break;
            case RUNNING:
                chosenMET = MET_RUNNING;
                startCounting();
                break;
            case CYCLING:
                chosenMET = MET_CYCLING;
                startCounting();
                break;
        }
    }

    private static void startCounting(){
        start = System.currentTimeMillis();
    }

    //https://www.omnicalculator.com/sports/calories-burned#how-to-calculate-calories-burned
    static double stopFitnessActivity(UserModel user){
        long fitnessActivityDuration = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - start);

        Log.d(TAG, "start time:" + start + ", total duration: " + fitnessActivityDuration);
        //85 is the weight that should be replaced with user.getWeight()
        return (fitnessActivityDuration * 60 * chosenMET * 3.5 * 85) /200;
    }



}
