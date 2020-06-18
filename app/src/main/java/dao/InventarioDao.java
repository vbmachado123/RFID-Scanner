package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Inventario;
import model.Leitura;

@Dao
public interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(Inventario inventario);

    @Update
    void atualizar(Inventario inventario);

    @Query("SELECT * FROM Inventario")
    Cursor carregarTodos();

    @Query("SELECT * FROM Inventario")
    List<Inventario> getAll();

    @Query("SELECT * FROM Inventario WHERE id = :id")
    Inventario pegaUm(int id);

    @Query("DELETE FROM inventario")
    void deleteAll();

}
