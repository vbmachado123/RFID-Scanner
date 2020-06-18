package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Equipamento;
import model.EquipamentoInventario;

@Dao
public interface EquipamentoInventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(EquipamentoInventario equipamento);

    @Update
    void atualizar(EquipamentoInventario equipamento);

    @Query("SELECT * FROM EquipamentoInventario")
    Cursor carregarTodos();

    @Query("SELECT * FROM EquipamentoInventario")
    List<EquipamentoInventario> getAll();

    @Query("SELECT * FROM EquipamentoInventario WHERE id = :id")
    EquipamentoInventario pegaUm(int id);

    @Query("DELETE FROM EquipamentoInventario")
    void deleteAll();
}
