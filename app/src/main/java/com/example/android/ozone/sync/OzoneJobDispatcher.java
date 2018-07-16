package com.example.android.ozone.sync;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class OzoneJobDispatcher extends JobService {

    private AsyncTask mBackgroundTask;
    //Start the job of updating dtat in the database
    @Override
    public boolean onStartJob(final JobParameters job) {
        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context= getApplicationContext();
               // OzoneSyncTask.ozoneTask(context);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                //Job is successful there is no need to reschedule
                jobFinished(job,false);
            }
        };
        mBackgroundTask.execute();
        return true;
    }

    //Stop job if for instance it started when user is was on WI-FI and in the middle of the job WI-FI shuts down.
    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null){
            mBackgroundTask.cancel(true);
        }
        //If conditions are met job kicks off again.
        return true;
    }
}
