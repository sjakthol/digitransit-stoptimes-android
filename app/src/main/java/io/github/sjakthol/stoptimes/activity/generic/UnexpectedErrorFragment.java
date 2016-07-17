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
public class UnexpectedErrorFragment extends MessageFragment {

    public UnexpectedErrorFragment() {
        super();

        setMessage(
            R.string.unexpected_error_title,
            R.string.unexpected_error_message
        );
    }
}
