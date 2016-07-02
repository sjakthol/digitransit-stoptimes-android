package io.github.sjakthol.stoptimes.activity.generic;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.sjakthol.stoptimes.R;

/**
 * A {@link Fragment} for showing errors and exceptions to the user.
 */
public class UnexpectedErrorFragment extends Fragment {
    public UnexpectedErrorFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unexpected_error, container, false);
    }

}
