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

@Dao
public interface InventarioNegadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(InventarioNegado inventarioNegado);

    @Update
    void atualizar(InventarioNegado inventarioNegado);

    @Query("SELECT * FROM InventarioNegado")
    Cursor carregarTodos();

    @Query("SELECT * FROM InventarioNegado")
    List<InventarioNegado> getAll();

    @Query("SELECT * FROM InventarioNegado WHERE id = :id")
    InventarioNegado pegaUm(int id);

    @Query("DELETE FROM InventarioNegado")
    void deleteAll();
}
