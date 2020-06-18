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
import model.SubLocal;

@Dao
public interface SubLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(SubLocal subLocal);

    @Update
    void atualizar(SubLocal subLocal);

    @Query("SELECT * FROM SubLocal")
    Cursor carregarTodos();

    @Query("SELECT * FROM SubLocal")
    List<SubLocal> getAll();

    @Query("SELECT * FROM SubLocal WHERE id = :id")
    SubLocal pegaUm(int id);

    @Query("DELETE FROM SubLocal")
    void deleteAll();
}
