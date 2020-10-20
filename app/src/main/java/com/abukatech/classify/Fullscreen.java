package com.abukatech.classify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fullscreen.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fullscreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fullscreen extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT = "text";

    private OnFragmentInteractionListener mListener;
    private static List<Data> newData;
    private static int newPosition;

    private Handler handler;
    private ImageButton backButton;
    private ImageButton viewleft;
    private TextView txtview;
    private ImageButton viewright;


    public Fullscreen() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Fullscreen newInstance(List<Data> data, int position) {
        Fullscreen fragment = new Fullscreen();
        Bundle args = new Bundle();

        newData = data;
        newPosition = position;

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
        View view = inflater.inflate(R.layout.fragment_properties, container, false);
        final SubsamplingScaleImageView fullScreen = view.findViewById(R.id.fullScreen);
        handler = new Handler();

        // sets a view for all the button variables
        backButton = view.findViewById(R.id.backButton);
        viewleft = view.findViewById(R.id.viewleft);
        viewright = view.findViewById(R.id.viewright);
        // sets a textview for the text variable
        txtview = view.findViewById(R.id.txtview);

        // default loading image
        fullScreen.setImage(ImageSource.resource(R.drawable.ic_launcher));
        // sets the textview to the name of the file
        txtview.setText(newData.get(newPosition).getName());

        // loads the full-res image
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // rotates the image to the correct orientation
                fullScreen.setOrientation(SubsamplingScaleImageView.ORIENTATION_90);
                // sets the image
                fullScreen.setImage(ImageSource.uri(newData.get(newPosition).getFilePath().getAbsolutePath()));

            }
        }, 300);

        // hides either one of the buttons if the image opened is either the first or last in the gallery
        if (newPosition == 0) {
            viewleft.setVisibility(View.INVISIBLE);
        } else if (newPosition == newData.size()-1) {
            viewright.setVisibility(View.INVISIBLE);
        }

        // goes back to the previous fragment
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stops the thread from running
                handler.removeCallbacksAndMessages(null);
                // hides the full screen image
                sendBack("sent back from fullscreen");
            }
        });

        // goes to the previous image in the galler
        viewleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(fullScreen, viewleft, newPosition-1);
            }
        });

        // goes to the next image in the gallery
        viewright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(fullScreen, viewright, newPosition+1);
            }
        });

        return view;
    }

    // changes the image in the fullscreen mode
    public void changeImage(SubsamplingScaleImageView view, ImageButton button, int pos) {

        // makes sure that the new position doesn't give an indexoutofbounds exception
        if (!(pos >= newData.size()) && !(pos < 0)) {
            newPosition = pos;

            // shows both the buttons before hiding one
            viewleft.setVisibility(View.VISIBLE);
            viewright.setVisibility(View.VISIBLE);

            // hides the button if it reaches the end or start of the gallery
            if (pos == 0 || pos == newData.size()-1) {
                button.setVisibility(View.INVISIBLE);
            }

                // sets the text
            txtview.setText(newData.get(newPosition).getName());
            // sets the image
            view.setImage(ImageSource.uri(newData.get(newPosition).getFilePath().getAbsolutePath()));
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void sendBack(String sendBackText) {
        if (mListener != null) {
            mListener.onFragmentInteraction(sendBackText);
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
        void onFragmentInteraction(String sendBackText);
    }
}
