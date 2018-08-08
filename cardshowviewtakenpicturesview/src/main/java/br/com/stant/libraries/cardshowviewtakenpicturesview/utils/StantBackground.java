package br.com.stant.libraries.cardshowviewtakenpicturesview.utils;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

public class StantBackground {

    public interface Background {
        void run();
    }


    public interface RunOnBackground {
        Object run();
    }

    public interface RunAfterExecute {
        void run(Object result);
    }
    public static void runOnBackground(Background background) {
        new Thread(() -> {
            background.run();
        }).start();
    }

    public static void runOnUiThread(FragmentActivity fragmentActivity, Background background) {
        if(fragmentActivity != null)
           fragmentActivity.runOnUiThread(() -> background.run());
    }
    public static <P,PR,R> void runOnAsyncTask(RunOnBackground onBackground, RunAfterExecute runAfterExecute ) {

        new AsyncTask<P, PR, R>() {

            @Override
            protected R doInBackground(P... ps) {
                return (R) onBackground.run();
            }

            @Override
            protected void onPostExecute(R r) {
                runAfterExecute.run(r);
                this.cancel(true);
                super.onPostExecute(r);
            }
        }.execute();
    }
}
