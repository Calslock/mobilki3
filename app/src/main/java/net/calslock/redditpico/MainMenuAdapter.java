package net.calslock.redditpico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.calslock.redditpico.app.VolleyCallback;

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.ViewHolder> {

    private String[][] localdataSet;
    private ItemClickListener itemClickListener;


    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView singleSubredditName;
        private final TextView singleKarma;
        private final TextView singleUsername;
        private final TextView singleTitle;

        public ViewHolder(View view) {
            super(view);
            singleSubredditName = (TextView) view.findViewById(R.id.singleSubredditName);
            singleKarma = (TextView) view.findViewById(R.id.singleKarma);
            singleUsername = (TextView) view.findViewById(R.id.singleUsername);
            singleTitle = (TextView) view.findViewById(R.id.singleTitle);
            view.setOnClickListener(this);
        }

        public TextView getSingleSubredditName() {
            return singleSubredditName;
        }

        public TextView getSingleKarma() {
            return singleKarma;
        }

        public TextView getSingleUsername() {
            return singleUsername;
        }

        public TextView getSingleTitle() {
            return singleTitle;
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[][] containing the data to populate views to be used
     * by RecyclerView.
     */
    public MainMenuAdapter(String[][] dataSet) {
        localdataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_post_main, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getSingleSubredditName().setText(localdataSet[position][0]);
        viewHolder.getSingleKarma().setText(localdataSet[position][1]);
        viewHolder.getSingleUsername().setText(localdataSet[position][2]);
        viewHolder.getSingleTitle().setText(localdataSet[position][3]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localdataSet.length;
    }

    // convenience method for getting data at click position
    String[] getItem(int id) {
        return localdataSet[id];
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
