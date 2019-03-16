package io.github.sjakthol.stoptimes.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * A singleton class that wraps Volley request queue for the duration
 * of the application lifetime.
 */
public class VolleyWrapper {
    @SuppressLint("StaticFieldLeak")
    private static VolleyWrapper mInstance;
    private static RequestQueue mRequestQueue;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private VolleyWrapper(Context ctx) {
        mContext = ctx;
    }

    /**
     * Fetch the Volley request queue to use for network requests.
     *
     * @return the singleton request queue
     */
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Get the singleton instance of the wrapper.
     *
     * @return the singleton wrapper
     */
    public static synchronized VolleyWrapper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new VolleyWrapper(ctx);
        }

        return mInstance;
    }
}