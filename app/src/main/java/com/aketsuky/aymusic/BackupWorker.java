package com.aketsuky.aymusic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackupWorker extends Worker {

    private static final String TAG = "BACKUP_WORKER_TAG";

    public BackupWorker (@NonNull Context context, @NonNull WorkerParameters workerParams ) {
        super ( context, workerParams );
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    @Override
    public Result doWork () {
        Intent myService = new Intent(WebAppInterface.mainActivity, MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MainActivity.actualWb.getContext().startForegroundService(myService);
        }
        return Result.success ();
    }
}