package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Local;
import model.Status;

@Dao
public interface StatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(Status status);

    @Update
    void atualizar(Status status);

    @Query("SELECT * FROM Status")
    Cursor carregarTodos();

    @Query("SELECT * FROM Status")
    List<Status> getAll();

    @Query("SELECT * FROM Status WHERE id = :id")
    Status pegaUm(int id);

    @Query("DELETE FROM Status")
    void deleteAll();
}
