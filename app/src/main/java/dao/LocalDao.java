package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.InventarioNegado;
import model.Local;

@Dao
public interface LocalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(Local local);

    @Update
    void atualizar(Local local);

    @Query("SELECT * FROM Local")
    public Local[] carregarTodos();

    @Query("DELETE FROM Local")
    void deleteAll();

}
