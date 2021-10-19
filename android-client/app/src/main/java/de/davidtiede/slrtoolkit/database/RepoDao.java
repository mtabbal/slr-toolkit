package de.davidtiede.slrtoolkit.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RepoDao {
    @Query("SELECT * FROM repo")
    LiveData<List<Repo>> getAllRepos();

    @Query("SELECT * FROM repo WHERE id=:id ")
    LiveData<Repo> getRepoById(int id);

    @Insert
    long insert(Repo repo);

    @Update
    void update(Repo repo);

    @Delete
    void delete(Repo repo);
}