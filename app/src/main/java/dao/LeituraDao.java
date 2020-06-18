package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Leitura;

@Dao
public interface LeituraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(Leitura l);

    @Update
    void atualizar(Leitura leitura);

    @Query("SELECT * FROM Leitura")
    Cursor carregarTodos();

    @Query("SELECT * FROM Leitura")
    List<Leitura> getAll();

    @Query("SELECT * FROM Leitura WHERE numeroTag = :numTag")
    Leitura pegaUm(String numTag);

    @Query("DELETE FROM Leitura")
    void deleteAll();
}
