package io.github.sjakthol.stoptimes.activity.generic;


import android.support.v4.app.Fragment;

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
