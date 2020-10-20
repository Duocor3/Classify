package com.abukatech.classify;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Gallery.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Gallery#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Gallery extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT = "text";

    private OnFragmentInteractionListener mListener;
    private static List<Data> newData;
    private static boolean isInfoScreen;
    HorizontalAdapter horizontalAdapter;

    public Gallery() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Gallery newInstance() {
        Gallery fragment = new Gallery();
        Bundle args = new Bundle();
        isInfoScreen = true;
        fragment.setArguments(args);
        return fragment;
    }

    public static Gallery newInstance(List<Data> data) {
        Gallery fragment = new Gallery();
        Bundle args = new Bundle();
        newData = data;
        isInfoScreen = false;
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
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        // gets the ID of the elements in the view
        final ImageButton backButton = view.findViewById(R.id.backButton);
        final TextView title = view.findViewById(R.id.title);

        ScrollView helpLayout = view.findViewById(R.id.helpLayout);

        if (!isInfoScreen) {
            // hides the info layouts
            helpLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // sets the title of the view
            title.setText("View");

            // sets up the recyclerview
            horizontalAdapter = new HorizontalAdapter(newData, getActivity().getApplication(), getActivity().getSupportFragmentManager());
            ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getActivity(), R.dimen.item_offset);

            final GridLayoutManager horizontalLayoutManager =
                    new GridLayoutManager(getActivity().getApplication(), 2, GridLayoutManager.VERTICAL, false);

            recyclerView.setLayoutManager(horizontalLayoutManager);
            recyclerView.addItemDecoration(itemDecoration);
            recyclerView.setAdapter(horizontalAdapter);

        } else {
            // shows the info layouts
            helpLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            // sets the title of the view
            title.setText("Help");

            // displays the text in the info page
            TextView info = view.findViewById(R.id.info);

            TextView more1 = view.findViewById(R.id.more1);

            ImageButton share = view.findViewById(R.id.share);
            RatingBar rating = view.findViewById(R.id.rating);

            TextView more2 = view.findViewById(R.id.more2);

            // sets the text for the help and contribute screens
            SpannableString str1 = styleText("Take pictures", " of your school assignments using the camera button. ", true);
            SpannableString str2 = styleText("Classify", " will autosort the pictures based on the time" +
                    " you took them.\n\n", false);
            SpannableString str3 = styleText("View", " any of the photos taken for a class by clicking on the" +
                    " respective period.\n\n", true);
            SpannableString str4 = styleText("Customize", " each folder by holding it or by using the edit" +
                    " button.\n\n", true);
            SpannableString str5 = styleText("Temp Folder", " stores the photos until they are " +
                    "sorted.\n\n", true);
            SpannableString str6 = styleText("Autosorting", " can be turned off in Settings, which will let" +
                    " you choose which class to save the picture to.\n\n", true);
            SpannableString str7 = styleText("Backup to Gallery", " will save any photos taken into the DCIM folder," +
                    " so photos won't be lost if the app is updated or uninstalled.\n\n", true);
            SpannableString str8 = styleText("Notifications", " remind you to check your assignments after" +
                    " school ends.", true);

            SpannableString str9 = styleText("t.beteselassie@gmail.com", "", false);

            info.setText(TextUtils.concat(str1, str2, str3, str4, str5, str6, str7, str8));

            more1.setText("Like the app? Share and leave a review on Google Play!");

            // makes the share intent pop up when you click the share icon
            share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);

                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            "Download Classify on Google Play! " +
                                    "http://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Share using"));
                }
            });

            rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    // sends the user to the google play "leave a rating" page
                    Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                    Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(myAppLinkToMarket);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), "App is not listed on the Play Store", Toast.LENGTH_LONG).show();
                    }
                }
            });

            more2.setText(TextUtils.concat("For questions and feedback, please contact the developer at ", str9));
        }

        // sets up an onclicklistener that takes the user to the main activity state
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TransitionDrawable)backButton.getBackground()).startTransition(300);
                sendBack("i was sent back");
                ((TransitionDrawable)backButton.getBackground()).reverseTransition(300);
            }
        });

        return view;
    }

    // makes part of a string either bold or italic
    private SpannableString styleText(String styleText, String normalText, boolean isBold) {
        SpannableString str = new SpannableString(styleText + normalText);

        if (isBold) {
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, styleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            str.setSpan(new StyleSpan(Typeface.ITALIC), 0, styleText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return str;
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
