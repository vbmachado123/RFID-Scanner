package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.EquipamentoInventario;

@Dao
public interface EquipamentoInventarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(EquipamentoInventario equipamento);

    @Update
    void atualizar(EquipamentoInventario equipamento);

    @Query("SELECT * FROM EquipamentoInventario")
    public EquipamentoInventario[] carregarTodos();

    @Query("DELETE FROM EquipamentoInventario")
    void deleteAll();
}
