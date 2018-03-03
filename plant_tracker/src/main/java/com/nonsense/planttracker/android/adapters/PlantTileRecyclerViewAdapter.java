package com.nonsense.planttracker.android.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nonsense.planttracker.R;
import com.nonsense.planttracker.android.activities.PlantTrackerUi;
import com.nonsense.planttracker.tracker.impl.Plant;

import java.io.File;
import java.util.List;

import static com.nonsense.planttracker.android.Utility.decodeSampledBitmapFromResource;

/**
 * Created by Derek Brooks on 7/1/2017.
 */

public class PlantTileRecyclerViewAdapter extends
        RecyclerView.Adapter<PlantTileRecyclerViewAdapter.ViewHolder> {

    private final ILongClickAction<Plant> longClickAction;
    private final IClickAction<Plant> clickAction;

    private Context context;
    private List<Plant> list;
    private PlantTrackerUi.IImageCache imageCache;

    class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView plantPreview;
        public TextView plantNameTextView;
        public TextView plantSummaryTextView;
        public TextView archivedTextView;

        public ViewHolder(View v) {
            super(v);

            plantPreview = (ImageView)v.findViewById(R.id.plantPreview);
            plantNameTextView = (TextView)v.findViewById(R.id.firstLine);
            plantSummaryTextView = (TextView)v.findViewById(R.id.secondLine);
            archivedTextView = (TextView)v.findViewById(R.id.archivedTextView);
        }
    }

    public interface IClickAction<T>   {
        public void clickAction(T o);
    }

    public interface ILongClickAction<T> {
        public void longClickAction(T o);
    }



    public PlantTileRecyclerViewAdapter(Context context, List<Plant> items,
                                        IClickAction<Plant> clickAction,
                                        ILongClickAction<Plant> longClickAction,
                                        PlantTrackerUi.IImageCache imageCache) {
        this.context = context;
        this.list = items;
        this.clickAction = clickAction;
        this.longClickAction = longClickAction;
        this.imageCache = imageCache;
    }

    @Override
    public PlantTileRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tile_plant_list,
                null);

        return new PlantTileRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlantTileRecyclerViewAdapter.ViewHolder viewHolder,
                                 int position) {

        final Plant p = list.get(position);

        if (p != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (p.getThumbnail() != null && p.getThumbnail() != "") {
                        Bitmap bitmap = imageCache.getImage(p.getThumbnail());
                        //decodeSampledBitmapFromResource(new File(p.getThumbnail()), 400, 300);

                        Runnable updateUi = new Runnable() {
                            @Override
                            public void run() {
                                viewHolder.plantPreview.setImageBitmap(bitmap);
                                viewHolder.plantPreview.setAlpha(0.5f);
                                Log.d("IPV", "End of loadThumb thread");
                            }
                        };

                        ((AppCompatActivity)context).runOnUiThread(updateUi);
                    }
                }
            };

            Thread loadImage = new Thread(runnable);
            loadImage.start();

            viewHolder.plantPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickAction.clickAction(p);
                }
            });

            viewHolder.plantPreview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickAction.longClickAction(p);
                    return true;
                }
            });

            if (viewHolder.plantNameTextView != null) {
                viewHolder.plantNameTextView.setText(p.getPlantName());
            }

            if (viewHolder.plantSummaryTextView != null) {
                String flowerWeek = "";

                viewHolder.plantSummaryTextView.setText("Started " + p.getDaysFromStart() +
                        " days ago, Grow Wk. " + p.getWeeksFromStart());
            }

            if (p.isArchived()) {
                viewHolder.archivedTextView.setVisibility(View.VISIBLE);
            }
            else    {
                viewHolder.archivedTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ((list!=null) ? list.size() : 0);
    }
}