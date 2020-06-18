package dao;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Equipamento;

@Dao
public interface EquipamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long inserir(Equipamento equipamento);

    @Update
    void atualizar(Equipamento equipamento);

    @Query("SELECT * FROM Equipamento")
    Cursor carregarTodos();

    @Query("SELECT * FROM Equipamento")
    List<Equipamento> getAll();

    @Query("SELECT * FROM Equipamento WHERE id = :id")
    Equipamento pegaUm(int id);

    @Query("DELETE FROM Equipamento")
    void deleteAll();
}
