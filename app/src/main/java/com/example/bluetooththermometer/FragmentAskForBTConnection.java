package com.example.bluetooththermometer;
/**
 * This fragment is shown if the devices bluetooth connection is
 * disabled to ask the user if he wants to eneable bluetooth...
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentAskForBTConnection extends DialogFragment {

    askForBTConnectionReturn gf;

    List<String> itemList = new ArrayList<>();

    public FragmentAskForBTConnection() {
        // Constructor must be empty....
    }

    public static FragmentAskForBTConnection newInstance(String titel) {
        FragmentAskForBTConnection frag = new FragmentAskForBTConnection();
        Bundle args = new Bundle();
        args.putString("titel", titel);
        frag.setArguments(args);
        return frag;
    }

    /**
     * THE INTERFACE
     * <p>
     * This is the interface used to pass data from the
     * this fragment to it's activity
     */
    public interface askForBTConnectionReturn {
        void getDialogInput(String buttonPressed);
    }

    /**
     * get interface Object...
     * You may use any method defined in the interface through the
     * object 'gf'
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        gf = (askForBTConnectionReturn) activity;
    }

    /**
     * Inflate fragment layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_device, container);
    }

    /**
     * This fills the layout with data.
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // When Cancel Button is pressed, finish!
        Button cancelButton = (Button) view.findViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gf.getDialogInput("CANCEL");
                dismiss();
            }
        });

        Button okButton=view.findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gf.getDialogInput("OK");
            }
        });
    }
}
