package com.abukatech.classify;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.util.List;

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

    private List<Data> horizontalList;
    private Context context;
    private FragmentManager childFragment;
    private Handler handler;

    public HorizontalAdapter(List<Data> horizontalList, Context context, FragmentManager childFragment) {
        this.horizontalList = horizontalList;
        this.context = context;
        this.childFragment = childFragment;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        SubsamplingScaleImageView imageView;
        TextView text;
        ImageButton zoom;
        ImageButton delete;

        public MyViewHolder(final View view) {
            super(view);

            handler = new Handler();
            imageView = view.findViewById(R.id.imageview);
            zoom = view.findViewById(R.id.zoom);
            delete = view.findViewById(R.id.delete);
            text = view.findViewById(R.id.txtview);

            // rotates the image to the correct orientation
            imageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            // crops the center and fills up the entire imageview
            imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);

            // allows you to either click on the image or press the zoom button to open
            zoom.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // sets the image
                    final File f = MainActivity.getData().get(getAdapterPosition()).getFilePath();
                    openImageFullscreen(getAdapterPosition());
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // sets the image
                    final File f = MainActivity.getData().get(getAdapterPosition()).getFilePath();
                    openImageFullscreen(getAdapterPosition());
                }
            });

            // makes the delete button delete the image it's attached to
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final File f = MainActivity.getData().get(getAdapterPosition()).getFilePath();
                    // deletes the current view
                    horizontalList.remove(getAdapterPosition());
                    f.delete();
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_main, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final File f = MainActivity.getData().get(holder.getAdapterPosition()).getFilePath();

        // default loading image + file name as a header
        holder.text.setText(horizontalList.get(holder.getAdapterPosition()).getName());
        holder.imageView.setImage(ImageSource.resource(R.mipmap.ic_launcher));

        // loads the recyclerview in a separate thread so the fragment animation can finish
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // sets the imageview and textview in the recyclerview
                holder.imageView.setImage(ImageSource.uri(f.getAbsolutePath()));
            }
        }, 300);
    }

    @Override
    public int getItemCount() {
        return horizontalList.size();
    }

    // opens the gallery in a new fragment
    private void openImageFullscreen(int position) {
        FragmentManager fragmentManager = childFragment;
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_from_top,
                R.anim.enter_from_bottom, R.anim.exit_from_top);

        Fullscreen fullscreen = Fullscreen.newInstance(MainActivity.getData(), position);
        transaction.add(R.id.properties_fragment, fullscreen, "Fullscreen").addToBackStack(null).commit();
    }
}