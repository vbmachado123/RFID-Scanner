package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.Local;
import model.Status;

@Dao
public interface StatusDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(Status status);

    @Update
    void atualizar(Status status);

    @Query("SELECT * FROM Status")
    public Status[] carregarTodos();

    @Query("DELETE FROM Status")
    void deleteAll();
}
