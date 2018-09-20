package com.example.android.ozone.utils.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;
// Most of the code has been used from Udacity course on Notifications and has been modified
// to meet the needs of this app

public class OzoneFireBaseJobDispatcher {
    private static final long MIN_PERIOD_MILLIS = 15 * 60 * 1000L;   // 15 minutes
    private static final int SYNC_INTERVAL_HOURS = 5;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 5;
    private static boolean sInitialized;
    private static final String OZONE_SYNC_TAG = "ozone_sync_tag";
    private static final int OZONE_JOBSCHEDULER_ID = 1;
    private static final long INTERVAL = TimeUnit.HOURS.toMillis(SYNC_INTERVAL_HOURS);

    //Schedule the JobService OzoneJobService
    public static void scheduleJobSync(@NonNull final Context context) {

//        Driver driver = new GooglePlayDriver(context);
//        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
//
//        Job syncOzoneJob = dispatcher.newJobBuilder()
//                .setService(OzoneJobService.class)
//                .setTag(OZONE_SYNC_TAG)
//                .setConstraints(Constraint.ON_ANY_NETWORK)
//                .setLifetime(Lifetime.FOREVER)
//                .setRecurring(true)
//                .setTrigger(Trigger.executionWindow(SYNC_INTERVAL_SECONDS,
//                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
//                .setReplaceCurrent(true)
//                .build();
//
//        dispatcher.schedule(syncOzoneJob);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(
                    new JobInfo.Builder(OZONE_JOBSCHEDULER_ID, new ComponentName(context, OzoneJobService.class.getName()))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPeriodic(3000)
                            .setPersisted(true)
                            .build());
        }
    }

    synchronized public static void initialize(Context context) {
        if (sInitialized) return;
        sInitialized = true;
        scheduleJobSync(context);
    }
}
