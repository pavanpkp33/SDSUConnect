package cs646.edu.sdsu.cs.connectemallTab.helpers;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Pkp on 3/16/2017.
 */

public class VolleyHandler {

    private static VolleyHandler volleyInstance = null;
    private RequestQueue reqQueue;

    public VolleyHandler() {
        reqQueue = Volley.newRequestQueue(ContextHelper.getAppContext());
    }

    public static VolleyHandler getInstance(){

        if(volleyInstance == null){
            volleyInstance = new VolleyHandler();
        }
        return volleyInstance;

    }

    public RequestQueue getReqQueue() {
        return reqQueue;
    }
}
