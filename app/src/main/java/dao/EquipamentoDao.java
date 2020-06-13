package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.Equipamento;


@Dao
public interface EquipamentoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(Equipamento equipamento);

    @Update
    void atualizar(Equipamento equipamento);

    @Query("SELECT * FROM Equipamento")
    public Equipamento[] carregarTodos();

    @Query("DELETE FROM Equipamento")
    void deleteAll();
}
