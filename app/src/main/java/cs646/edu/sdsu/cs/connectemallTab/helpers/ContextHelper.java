package cs646.edu.sdsu.cs.connectemallTab.helpers;

import android.app.Application;
import android.content.Context;

/**
 * Created by Pkp on 3/16/2017.
 */

public class ContextHelper extends Application {

    private static Context appContext;

    public ContextHelper(Context appContext) {
        this.appContext = appContext;
    }

    public static Context getAppContext() {
        return appContext;
    }
}
