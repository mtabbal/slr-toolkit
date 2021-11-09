package de.davidtiede.slrtoolkit.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import org.jbibtex.BibTeXObject;
import org.jbibtex.Key;
import org.jbibtex.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import de.davidtiede.slrtoolkit.database.AppDatabase;
import de.davidtiede.slrtoolkit.database.Entry;
import de.davidtiede.slrtoolkit.database.EntryDao;
import de.davidtiede.slrtoolkit.database.Repo;
import de.davidtiede.slrtoolkit.util.BibTexParser;
import de.davidtiede.slrtoolkit.util.FileUtil;

public class EntryRepository {
    private EntryDao entryDao;
    Application application;
    FileUtil fileUtil;

    public EntryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        entryDao = db.entryDao();
        this.application  = application;
        fileUtil = new FileUtil();
    }


    public LiveData<List<Entry>> getEntryForRepo(int id) {
        return entryDao.getEntriesForRepo(id);
    }

    public void update(Entry entry) {
        AppDatabase.databaseWriteExecutor.execute(() -> entryDao.update(entry));
    }

    public long insert(Entry entry) {
        Callable<Long> insertCallable = () -> entryDao.insert(entry);
        long id = 0;

        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(insertCallable);
        try {
            id = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void delete(Entry entry, Repo repo) {
        String path = repo.getLocal_path();
        File file = fileUtil.accessFiles(path, application);
        try {
            BibTexParser parser = BibTexParser.getBibTexParser();
            parser.setBibTeXDatabase(file);
            Key key = new Key(entry.getKey());
            BibTeXObject entryToDelete =  parser.getBibTexObject(key);
            parser.removeObject(entryToDelete);
            AppDatabase.databaseWriteExecutor.execute(() -> entryDao.delete(entry));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public LiveData<Entry> getEntryByKey(int repoId, String key) {
        return entryDao.getEntryByKey(repoId, key);
    }

    public LiveData<Integer> getEntryAmountForRepo(int repoId) {
        return entryDao.getEntryAmount(repoId);
    }

    public LiveData<Integer> getEntryAmountForStatus(int repoId, Entry.Status status) {
        int statusCode = status.getCode();
        return entryDao.getEntryAmountForStatus(repoId, statusCode);
    }

    public LiveData<List<Entry>> getEntryForRepoByStatus(int repoId, Entry.Status status) {
        int statusCode = status.getCode();
        return entryDao.getEntryForRepoByStatus(repoId, statusCode);
    }
}
