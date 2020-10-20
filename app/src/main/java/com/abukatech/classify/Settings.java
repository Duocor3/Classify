package com.abukatech.classify;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Settings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Settings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT = "text";

    private OnFragmentInteractionListener mListener;
    private ImageButton backButton;
    private Button clickout;
    private Switch autoSort, backup, notifs;
    private static boolean newIsAutoSorting, newIsBackupEnabled, newIsNotifsEnabled;

    public Settings() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Settings newInstance(boolean isAutoSorting, boolean isBackupEnabled, boolean isNotifsEnabled) {
        Settings fragment = new Settings();
        Bundle args = new Bundle();
        newIsAutoSorting = isAutoSorting;
        newIsBackupEnabled = isBackupEnabled;
        newIsNotifsEnabled = isNotifsEnabled;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mText = getArguments().getString(TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // sets a button id for the "back" button
        backButton = view.findViewById(R.id.backButton);
        // sets a button id for the "edit" button
        autoSort = view.findViewById(R.id.autoSort);
        // sets button id for the "backup to gallery" switch
        backup = view.findViewById(R.id.backup);
        // sets button id for the "toggle notifs" button
        notifs = view.findViewById(R.id.togglenotifs);
        // sets a button for the clickout feature
        clickout = view.findViewById(R.id.clickout);

        // saves the values set by the user
        autoSort.setChecked(newIsAutoSorting);
        backup.setChecked(newIsBackupEnabled);
        notifs.setChecked(newIsNotifsEnabled);

        // toggles isautosorting with the switch
        autoSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newIsAutoSorting = autoSort.isChecked();
            }
        });

        // sets event listener for the "backup to gallery" switch
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newIsBackupEnabled = backup.isChecked();
            }
        });

        // toggles notifications
        notifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newIsNotifsEnabled = notifs.isChecked();
            }
        });

        // hides settings fragment if focus is lost
        clickout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBack(newIsAutoSorting, newIsBackupEnabled, newIsNotifsEnabled);
            }
        });

        // sets up an onclicklistener that takes the user to the main activity state
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TransitionDrawable)backButton.getBackground()).startTransition(300);
                sendBack(newIsAutoSorting, newIsBackupEnabled, newIsNotifsEnabled);
                ((TransitionDrawable)backButton.getBackground()).reverseTransition(300);
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void sendBack(boolean isAutoSorting, boolean backupEnabled, boolean notifsEnabled) {
        if (mListener != null) {
            mListener.onFragmentInteraction(isAutoSorting, backupEnabled, notifsEnabled);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(boolean isAutoSorting, boolean isBackupEnabled, boolean isNotifsEnabled);
    }
}
