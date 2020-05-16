package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.Inventario;

@Dao
public interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(Inventario inventario);

    @Update
    void atualizar(Inventario inventario);

    @Query("SELECT * FROM inventario")
    public Inventario[] carregarTodos();

    @Query("DELETE FROM inventario")
    void deleteAll();

}
