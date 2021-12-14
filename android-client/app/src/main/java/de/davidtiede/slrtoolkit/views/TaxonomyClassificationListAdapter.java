package de.davidtiede.slrtoolkit.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.davidtiede.slrtoolkit.R;
import de.davidtiede.slrtoolkit.database.TaxonomyWithEntries;

public class TaxonomyClassificationListAdapter extends ListAdapter<TaxonomyWithEntries, TaxonomyClassificationListAdapter.TaxonomyClassificationViewHolder> {
    private TaxonomyClassificationListAdapter.RecyclerViewClickListener listener;
    private RecyclerView recyclerView;
    private int entryId;
    List<Integer> currentTaxonomyIds;

    public TaxonomyClassificationListAdapter(@NonNull DiffUtil.ItemCallback<TaxonomyWithEntries> diffCallback, TaxonomyClassificationListAdapter.RecyclerViewClickListener listener, int entryId) {
        super(diffCallback);
        this.listener = listener;
        this.entryId = entryId;
        this.currentTaxonomyIds = new ArrayList<>();
    }

    public void setCurrentTaxonomyIds(List<Integer> currentTaxonomyIds) {
        this.currentTaxonomyIds = currentTaxonomyIds;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public Context getContext() {
        return recyclerView.getContext();
    }

    @NonNull
    @Override
    public TaxonomyClassificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TaxonomyClassificationViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxonomyClassificationViewHolder holder, int position) {
        TaxonomyWithEntries current = getItem(position);
        holder.bind(current, listener, entryId, currentTaxonomyIds);
    }

    public TaxonomyWithEntries getItemAtPosition(int position) {
        TaxonomyWithEntries taxonomy = getItem(position);
        return taxonomy;
    }

    public static class TaxonomyClassificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView taxonomyItemView;
        private TaxonomyClassificationListAdapter.RecyclerViewClickListener listener;

        public TaxonomyClassificationViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            taxonomyItemView = itemView.findViewById(R.id.textview_recyclerview);
            itemView.setOnClickListener(this);
        }

        public static TaxonomyClassificationListAdapter.TaxonomyClassificationViewHolder create(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recyclerview_item, parent, false);
            return new TaxonomyClassificationListAdapter.TaxonomyClassificationViewHolder(view);
        }


        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }

        public void bind(TaxonomyWithEntries taxonomy, TaxonomyClassificationListAdapter.RecyclerViewClickListener listener, int entryId, List<Integer> selectedTaxonomyIds) {
            boolean taxonomyInEntry = false;
            for(int taxonomyId: selectedTaxonomyIds) {
                if(taxonomyId == taxonomy.taxonomy.getTaxonomyId()) {
                    taxonomyInEntry = true;
                }
            }
            if(taxonomyInEntry) {
                taxonomyItemView.setBackgroundColor(Color.BLUE);
            } else {
                taxonomyItemView.setBackgroundColor(Color.WHITE);
            }
            taxonomyItemView.setText(taxonomy.taxonomy.getName());
            this.listener = listener;
        }
    }

    public interface RecyclerViewClickListener {
        void onClick(View v, int position);
    }

    public static class TaxonomyDiff extends DiffUtil.ItemCallback<TaxonomyWithEntries> {
        @Override
        public boolean areItemsTheSame(@NonNull TaxonomyWithEntries oldItem, @NonNull TaxonomyWithEntries newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TaxonomyWithEntries oldItem, @NonNull TaxonomyWithEntries newItem) {
            return false;
        }
    }
}