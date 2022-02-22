package de.davidtiede.slrtoolkit.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.davidtiede.slrtoolkit.R;
import de.davidtiede.slrtoolkit.database.Entry;
import de.davidtiede.slrtoolkit.viewmodels.ProjectViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class EntriesToClassifyViewPagerFragment extends Fragment {

    ViewPager2 viewPager;
    ProjectViewModel projectViewModel;
    private FragmentStateAdapter pagerAdapter;
    private TextView noEntriesToClassifyTextview;
    int repoId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entries_to_classify_view_pager, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        repoId = projectViewModel.getCurrentRepoId();
        viewPager = view.findViewById(R.id.classify_entries_viewpager);
        noEntriesToClassifyTextview = view.findViewById(R.id.textview_no_entries_to_classify);
        pagerAdapter = new EntrySlidePagerAdapter(EntriesToClassifyViewPagerFragment.this.getActivity(), new ArrayList<Entry>());
        viewPager.setAdapter(pagerAdapter);

        projectViewModel.getEntriesWithoutTaxonomies(repoId).observe(getViewLifecycleOwner(), new Observer<List<Entry>>() {
            @Override
            public void onChanged(List<Entry> entries) {
                if(entries.size() == 0) {
                    noEntriesToClassifyTextview.setVisibility(View.VISIBLE);
                } else {
                    noEntriesToClassifyTextview.setVisibility(View.INVISIBLE);
                    pagerAdapter = new EntrySlidePagerAdapter(EntriesToClassifyViewPagerFragment.this.getActivity(), entries);
                    viewPager.setAdapter(pagerAdapter);
                }
            }
        });
    }

    public class EntrySlidePagerAdapter extends FragmentStateAdapter {
        List<Entry> entries;

        public EntrySlidePagerAdapter(FragmentActivity fa, List<Entry> entries) {
            super(fa);
            this.entries = entries;
        }

        @Override
        public Fragment createFragment(int position) {
            Entry entry = entries.get(position);
            projectViewModel.setCurrentEntryIdForCard(entry.getId());
            Fragment fragment = new BibtexEntryDetailFragment();
            return fragment;
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }
    }
}