package io.github.sjakthol.stoptimes.activity.generic;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import io.github.sjakthol.stoptimes.R;
import io.github.sjakthol.stoptimes.utils.Logger;


/**
 * A fragment for indicating that something is loading.
 */
public class LoadingFragment extends Fragment {
    private static final String TAG = LoadingFragment.class.getSimpleName();
    private static final String ARG_MSG = "ARG_MSG";

    /**
     * Create a new loading fragment with custom loading message
     *
     * @param msg the message to show
     * @return new LoadingFragment instance
     */
    public static LoadingFragment createWithMessage(String msg) {
        LoadingFragment f = new LoadingFragment();

        Bundle args = new Bundle();
        args.putString(ARG_MSG, msg);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG, "onResume()");

        TextView tv = (TextView) getView().findViewById(R.id.loading_text);

        if (getArguments() == null) {
            tv.setText(R.string.loading_default);
        } else {
            tv.setText(getArguments().getString(ARG_MSG));
        }
    }
}
