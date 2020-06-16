package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.EquipamentoInventario;
import model.InventarioNegado;
import model.Local;

@Dao
public interface LocalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long inserir(Local local);

    @Update
    void atualizar(Local local);

    @Query("SELECT * FROM Local")
    Cursor carregarTodos();

    @Query("SELECT * FROM Local")
    List<Local> getAll();

    @Query("SELECT * FROM Local WHERE id = :id")
    Local pegaUm(int id);

    @Query("DELETE FROM Local")
    void deleteAll();

}
