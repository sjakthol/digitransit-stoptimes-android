package io.github.sjakthol.stoptimes.activity.generic;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import io.github.sjakthol.stoptimes.R;

/**
 * A simple {@link Fragment} for showing a message with a
 * title and details.
 */
public class MessageFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String ARG_DETAILS = "details";

    public MessageFragment() {}

    /**
     * Create a new fragment with the given message.
     *
     * @param title the title of the message
     * @param details the details of the message
     *
     * @return a new MessageFragment that shows the given message
     */
    public static MessageFragment withMessage(@StringRes int title, @StringRes int details) {
        MessageFragment fragment = new MessageFragment();
        fragment.setMessage(title, details);
        return fragment;
    }

    /**
     * Set the message for this Fragment. MUST be called before the fragment is
     * attached to an activity.
     *
     * @param title the title of the message
     * @param details the details of the message
     */
    void setMessage(@StringRes int title, @StringRes int details) {
        assert getActivity() == null;

        Bundle args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_DETAILS, details);
        setArguments(args);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_message, container, false);

        TextView title = root.findViewById(R.id.title);
        TextView details = root.findViewById(R.id.details);

        Bundle args = getArguments();
        title.setText(args.getInt(ARG_TITLE));
        details.setText(args.getInt(ARG_DETAILS));

        return root;
    }

}
