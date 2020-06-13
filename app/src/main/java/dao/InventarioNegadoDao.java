package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.EquipamentoInventario;
import model.InventarioNegado;

@Dao
public interface InventarioNegadoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(InventarioNegado inventarioNegado);

    @Update
    void atualizar(InventarioNegado inventarioNegado);

    @Query("SELECT * FROM InventarioNegado")
    public InventarioNegado[] carregarTodos();

    @Query("DELETE FROM InventarioNegado")
    void deleteAll();
}
