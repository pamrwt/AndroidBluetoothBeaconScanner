package com.example.biaconscanner;

import android.app.Application;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class AnalyticsApplication extends Application {
    private Tracker mTracker;

    public synchronized Tracker getDefaultTracker() {
        if (this.mTracker == null) {
            this.mTracker = GoogleAnalytics.getInstance(this).newTracker((int) R.xml.global_tracker);
        }
        return this.mTracker;
    }
}
