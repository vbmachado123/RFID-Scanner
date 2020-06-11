package util;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Victor on 10/06/20.
 */
public class Preferencias {

    private Context contexto;
    private SharedPreferences preferences;
    private final String NOME_ARQUIVO = "sosRFiD.preferencias";
    private final int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String CHAVE_IDENTIFICADOR = "identificadorUsuarioLogado";
    private final String CHAVE_NOME = "nomeUsuarioLogado";
    private boolean CHAVE_CONEXAO = false;

    public Preferencias(Context contextoParametro) {
        contexto = contextoParametro;
        preferences = contexto.getSharedPreferences(NOME_ARQUIVO, MODE);
        editor = preferences.edit();
    }

    public void salvarDados(String identificadorUsuario, String  nomeUsuario, String estadoConexao) {

        editor.putString(CHAVE_IDENTIFICADOR, identificadorUsuario);
        editor.putString(CHAVE_NOME, nomeUsuario);
        editor.putString(String.valueOf(CHAVE_CONEXAO), estadoConexao);
        editor.commit();

    }

    public void salvarConexao(boolean estadoConexao){
        editor.putBoolean(String.valueOf(CHAVE_CONEXAO), estadoConexao);
        editor.commit();
    }

    public String getIdentificador(){
        return preferences.getString(CHAVE_IDENTIFICADOR, null);
    }

    public String getNome(){
        return preferences.getString(CHAVE_NOME, null);
    }

    public boolean getConexao(){
        return preferences.getBoolean(String.valueOf(CHAVE_CONEXAO), false);
    }

}
