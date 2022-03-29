package de.davidtiede.slrtoolkit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.davidtiede.slrtoolkit.R;
import de.davidtiede.slrtoolkit.database.Entry;
import de.davidtiede.slrtoolkit.database.TaxonomyWithEntries;
import de.davidtiede.slrtoolkit.viewmodels.TaxonomiesViewModel;
import de.davidtiede.slrtoolkit.views.BibTexEntriesListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TaxonomyEntriesListFragment extends Fragment {
    private static TaxonomiesViewModel taxonomiesViewModel;
    private BibTexEntriesListAdapter bibTexEntriesListAdapter;
    private BibTexEntriesListAdapter.RecyclerViewClickListener listener;
    private TextView noTaxonomyEntriesTextview;
    private TextView taxonomiesBreadCrumbTextview;
    int repoId;
    int currentTaxonomyId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_taxonomy_entries_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setOnClickListener();
        taxonomiesViewModel = new ViewModelProvider(requireActivity()).get(TaxonomiesViewModel.class);
        repoId = taxonomiesViewModel.getCurrentRepoId();
        currentTaxonomyId = taxonomiesViewModel.getCurrentTaxonomyId();
        RecyclerView taxonomyEntriesRecyclerView = view.findViewById(R.id.taxonomyEntriesRecyclerview);
        noTaxonomyEntriesTextview = view.findViewById(R.id.textview_no_taxonomy_entries);
        taxonomiesBreadCrumbTextview = view.findViewById(R.id.textview_entries_taxonomies_breadcrumb);
        taxonomyEntriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        bibTexEntriesListAdapter = new BibTexEntriesListAdapter(new BibTexEntriesListAdapter.EntryDiff(), listener, repoId);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(taxonomyEntriesRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        taxonomyEntriesRecyclerView.addItemDecoration(dividerItemDecoration);
        taxonomyEntriesRecyclerView.setAdapter(bibTexEntriesListAdapter);

        taxonomiesViewModel.getTaxonomyWithEntries(repoId, currentTaxonomyId).observe(getViewLifecycleOwner(), this::onLoaded);
        setHeader();
    }

    public void setHeader() {
        if(currentTaxonomyId > 0) {
            taxonomiesViewModel.getTaxonomyWithEntries(repoId, currentTaxonomyId).observe(getViewLifecycleOwner(), t -> {
                String path = t.taxonomy.getPath();
                if(path.length() > 1) {
                    path = path.replaceAll("#", " > ");
                    if (path.charAt(1) == ">".charAt(0)) {
                        path = path.replaceFirst(" > ", "");
                    }
                    path = getResources().getString(R.string.entries_for_taxonomy) + "\n" + path;
                    taxonomiesBreadCrumbTextview.setText(path);
                }
            });
        }
    }

    public void onLoaded(TaxonomyWithEntries taxonomyWithEntries) {
        List<Entry> entries = taxonomyWithEntries.entries;
        taxonomiesViewModel.setCurrentEntriesInList(entries);
        if(entries.size() == 0) {
            noTaxonomyEntriesTextview.setVisibility(View.VISIBLE);
        } else {
            noTaxonomyEntriesTextview.setVisibility(View.INVISIBLE);
            bibTexEntriesListAdapter.submitList(entries);
        }
    }

    private void setOnClickListener() {
        listener = (v, position) -> {
            Entry clickedEntry = bibTexEntriesListAdapter.getItemAtPosition(position);
            if(clickedEntry == null) return;

            taxonomiesViewModel.setCurrentEntryIdForCard(clickedEntry.getEntryId());
            int indexOfEntryInOriginalList = taxonomiesViewModel.getCurrentEntriesInList().indexOf(clickedEntry);
            taxonomiesViewModel.setCurrentEntryInListCount(indexOfEntryInOriginalList);
            Fragment entryFragment = new BibtexEntriesDetailViewPagerFragment();
            FragmentTransaction ft = TaxonomyEntriesListFragment.this.getParentFragmentManager().beginTransaction();
            ft.replace(R.id.taxonomies_fragment_container_view, entryFragment);
            ft.addToBackStack(null);
            ft.commit();
        };
    }
}