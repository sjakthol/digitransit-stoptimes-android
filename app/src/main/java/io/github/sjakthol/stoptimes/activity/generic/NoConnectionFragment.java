package io.github.sjakthol.stoptimes.activity.generic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Helpers;
import io.github.sjakthol.stoptimes.utils.Logger;

/**
 * A fragment that displays no connection message.
 */
public class NoConnectionFragment extends MessageFragment {
    private static final String TAG = NoConnectionFragment.class.getSimpleName();

    public NoConnectionFragment() {
        super();

        setMessage(
            R.string.nca_title,
            R.string.nca_details
        );
    }
    /**
     * An activity that listens for OnConnectionAvailable events.
     */
    private OnConnectionAvailable mListener;

    /**
     * A broadcast receiver used to receive network status changes.
     */
    private BroadcastReceiver mReceiver;

    /**
     * An interface for receiving a notification when connection is available.
     */
    public interface OnConnectionAvailable {
        /**
         * Called when connection has been established.
         */
        void onConnectionAvailable();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "Attaching to %s", context.toString());

        if (context instanceof OnConnectionAvailable) {
            mListener = (OnConnectionAvailable) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement Hooks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Logger.d(TAG, "onDetach()");

        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        assert mReceiver == null;
        mReceiver = registerNetworkStatusReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause()");

        if (mReceiver != null) {
            Logger.i(TAG, "Unregistering CONNECTIVITY_ACTION receiver");
            getActivity().unregisterReceiver(mReceiver);
        }

        mReceiver = null;
    }

    /**
     * Registers a BroadcastReceiver for CONNECTIVITY_ACTION.
     *
     * @return the new receiver
     */
    private BroadcastReceiver registerNetworkStatusReceiver() {
        Logger.i(TAG, "Registering CONNECTIVITY_ACTION receiver");

        // Filter the connectivity action
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        // Create a receiver
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                throw new AssertionError("Unexpected action " + intent.getAction());

            Logger.d(TAG, "Received CONNECTIVITY_ACTION event");
            if (Helpers.isConnected(context)) {
                Logger.i(TAG, "Got an internet connection; notifying parent");

                if (mListener == null) throw new AssertionError("No listener available");
                mListener.onConnectionAvailable();
            }
            }
        };

        // Register it
        getActivity().registerReceiver(receiver, filter);

        // Return the receiver so that it can later be removed
        return receiver;
    }
}
