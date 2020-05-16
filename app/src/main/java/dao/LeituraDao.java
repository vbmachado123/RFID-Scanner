package dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import model.Leitura;

@Dao
public interface LeituraDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserir(Leitura leitura);

    @Update
    void atualizar(Leitura leitura);

    @Query("SELECT * FROM leitura")
    public Leitura[] carregarTodos();

    //@Query("SELECT * FROM leitura WHERE numeroTag = " + :numeroTag)
    public Leitura pegaUm(String numerotag);

    @Query("DELETE FROM leitura")
    void deleteAll();
}
