package telas;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.rfidscanner.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import adapter.EquipamentoAdapter;
import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import model.Equipamento;
import model.EquipamentoInventario;

public class AlterarDescricaoActivity extends AppCompatActivity {

    private ArrayList<Equipamento> equipamentos;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private EquipamentoDao equipamentoDao;
    private EquipamentoAdapter adapter;
    private EquipamentoInventario equipamentoInventario = new EquipamentoInventario();
    private ListView lvEquipamentos;
    private FloatingActionButton fabSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar_descricao);

        equipamentos = new ArrayList<>();

        equipamentoInventarioDao = new EquipamentoInventarioDao(this);
        equipamentoDao = new EquipamentoDao(this);

        validaCampo();
        copulaLista();
    }

    private void validaCampo() {
        lvEquipamentos = (ListView) findViewById(R.id.lvEquipamentos);
        fabSalvar = (FloatingActionButton) findViewById(R.id.botaoSalvar);
        adapter = new EquipamentoAdapter(this, equipamentos);

        lvEquipamentos.setAdapter(adapter);

        lvEquipamentos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Equipamento equipamento = equipamentos.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(AlterarDescricaoActivity.this, R.style.Dialog);
                builder.setTitle("Equipamento");
                builder.setMessage(equipamento.getNumeroTag());
                builder.setIcon(R.drawable.ic_inventario);

                final EditText input = new EditText(AlterarDescricaoActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setTextColor(R.color.colorPrimary);
                // input.setHighlightColor(R.color.colorPrimary);
                input.setText(equipamento.getDescricao());
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_text = input.getText().toString();
                        EquipamentoDao equipamentoDao = new EquipamentoDao(AlterarDescricaoActivity.this);
                        equipamento.setDescricao(m_text);
                        equipamentoDao.atualizar(equipamento);
                        Toast.makeText(AlterarDescricaoActivity.this, "Descrição atualizada!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                Dialog dialog = builder.create();
                dialog.show();
            }
        });

        fabSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(AlterarDescricaoActivity.this, ListaInventarioActivity.class);
                startActivity(it);
                finish();
            }
        });
    }

    private void copulaLista() {
        ArrayList<EquipamentoInventario> equipamentoInventarioList = (ArrayList<EquipamentoInventario>) equipamentoInventarioDao.obterTodos();

        for (int i = 0; i < equipamentoInventarioList.size(); i++) {
            EquipamentoInventario ei = equipamentoInventarioList.get(i);
            Equipamento equipamento = equipamentoDao.getById(ei.getIdEquipamento());
            if (equipamento != null)
                equipamentos.add(equipamento);

            Log.i("Salvando", "EquipamentoInventario - " + equipamento.getNumeroTag());
        }

    }
}