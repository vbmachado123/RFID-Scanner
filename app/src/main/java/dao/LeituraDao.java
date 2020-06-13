package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import model.Leitura;

@Dao
public interface LeituraDao {

    @Insert
    Long inserir(Leitura l);

    @Update
    void atualizar(Leitura leitura);

    @Query("SELECT * FROM leitura")
    public Leitura[] carregarTodos();

    @Query("SELECT * FROM leitura")
    List<Leitura> getAll();

    @Query("SELECT * FROM leitura WHERE numeroTag = :numTag")
    public Leitura pegaUm(String numTag);

    @Query("DELETE FROM leitura")
    void deleteAll();
}
