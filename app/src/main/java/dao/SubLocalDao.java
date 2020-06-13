package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.Status;
import model.SubLocal;

@Dao
public interface SubLocalDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(SubLocal subLocal);

    @Update
    void atualizar(SubLocal subLocal);

    @Query("SELECT * FROM SubLocal")
    public SubLocal[] carregarTodos();

    @Query("DELETE FROM SubLocal")
    void deleteAll();
}
