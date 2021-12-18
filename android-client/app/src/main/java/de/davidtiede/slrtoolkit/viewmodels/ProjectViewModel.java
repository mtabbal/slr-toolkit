package de.davidtiede.slrtoolkit.viewmodels;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.davidtiede.slrtoolkit.database.Entry;
import de.davidtiede.slrtoolkit.database.Repo;
import de.davidtiede.slrtoolkit.database.Taxonomy;
import de.davidtiede.slrtoolkit.database.TaxonomyWithEntries;
import de.davidtiede.slrtoolkit.repositories.EntryRepository;
import de.davidtiede.slrtoolkit.repositories.RepoRepository;
import de.davidtiede.slrtoolkit.repositories.TaxonomyRepository;
import de.davidtiede.slrtoolkit.util.BibTexParser;
import de.davidtiede.slrtoolkit.util.FileUtil;

public class ProjectViewModel extends AndroidViewModel {
    private final RepoRepository repoRepository;
    private final EntryRepository entryRepository;
    private final TaxonomyRepository taxonomyRepository;
    private Application application;
    private int currentRepoId;
    private int currentEntryIdForCard;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        repoRepository = new RepoRepository(application);
        entryRepository = new EntryRepository(application);
        taxonomyRepository = new TaxonomyRepository(application);
        this.application = application;
    }

    public int getCurrentRepoId() {
        return currentRepoId;
    }

    public void setCurrentRepoId(int currentRepoId) {
        this.currentRepoId = currentRepoId;
    }

    public int getCurrentEntryIdForCard() {
        return currentEntryIdForCard;
    }

    public void setCurrentEntryIdForCard(int currentEntryIdForCard) {
        this.currentEntryIdForCard = currentEntryIdForCard;
    }

    public LiveData<Integer> getEntryAmount(int repoId) {
        return entryRepository.getEntryAmountForRepo(repoId);
    }

    public LiveData<Integer> getOpenEntryAmount(int repoId) {
        return entryRepository.getEntryAmountForStatus(repoId, Entry.Status.OPEN);
    }

    public LiveData<Repo> getRepoById(int id) {
        return repoRepository.getRepoById(id);
    }

    public void saveAll(List<Entry> entries) {
       entryRepository.saveAll(entries);
    }

    public void initializeData(int repoId, String path) {
        FileUtil fileUtil = new FileUtil();
        File file = fileUtil.accessFiles(path, application, ".bib");
        try {
            BibTexParser parser = BibTexParser.getBibTexParser();
            parser.setBibTeXDatabase(file);
            Map<Entry, String> entriesWithTaxonomies = parser.parseBibTexFile(file, repoId);
            List<Entry> entries = new ArrayList<>();
            for(Entry entry : entriesWithTaxonomies.keySet()) {
                entries.add(entry);
            }
            System.out.println("What entries are we trying to save?");
            for(Entry e: entries) {
                System.out.println(e.getTitle());
            }
            saveAll(entries);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void initializeTaxonomy(int repoId, String path) {
        taxonomyRepository.initializeTaxonomy(repoId, path, application);
    }

    public LiveData<List<Entry>> getEntriesForRepo(int repoId) {
        return entryRepository.getEntryForRepo(repoId);
    }

    public Repo getRepoByIdDirectly(int id) {
        Repo repo = null;
        try {
            repo = repoRepository.getRepoByIdDirectly(id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return repo;
    }

    public void delete(Entry entry, int id) {
        Repo repo = getRepoByIdDirectly(id);
        if(repo != null) {
            entryRepository.delete(entry, repo);
        }
    }

    public LiveData<Entry> getEntryById(int id) {
        return entryRepository.getEntryById(id);
    }

    public LiveData<List<Entry>> getOpenEntriesForRepo(int repoId) {
        return entryRepository.getEntryForRepoByStatus(repoId, Entry.Status.OPEN);
    }

    public void updateEntry(Entry entry) {
        entryRepository.update(entry);
    }

    public LiveData<TaxonomyWithEntries> getTaxonomyWithEntries(int repoId, int taxonomyId) {
        return taxonomyRepository.getTaxonomyWithEntries(repoId, taxonomyId);
    }
}
