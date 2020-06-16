package util;

import android.content.Context;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFAnchor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import javax.net.ssl.SSLEngineResult;

import helper.EquipamentoHelper;
import helper.EquipamentoInventarioHelper;
import helper.InventarioHelper;
import helper.InventarioNegadoHelper;
import helper.LeituraHelper;
import helper.LocalHelper;
import helper.StatusHelper;
import helper.SubLocalHelper;
import model.Equipamento;
import model.Leitura;
import model.Local;
import model.Status;
import model.SubLocal;
import sql.Database;

public class Xlsx {

    private static final String TAG = "Importacao";
    private Context context;

    /* BANCO DE DADOS */
    private Database db;
    private EquipamentoHelper equipamentoHelper;
    private EquipamentoInventarioHelper equipamentoInventarioHelper;
    private InventarioHelper inventarioHelper;
    private InventarioNegadoHelper inventarioNegadoHelper;
    private LeituraHelper leituraHelper;
    private LocalHelper localHelper;
    private StatusHelper statusHelper;
    private SubLocalHelper subLocalHelper;

    public Xlsx(Context context) {
        this.context = context;
        this.db = Database.getDatabase(context);
        leituraHelper = new LeituraHelper(context);
        localHelper = new LocalHelper(context);
        subLocalHelper = new SubLocalHelper(context);
        equipamentoHelper = new EquipamentoHelper(context);
        statusHelper = new StatusHelper(context);
    }

    /* Método responsável por importar a tabela - Converter e Salvar no banco*/
    public boolean importarTabela(File filePath) {
        boolean importar = false;

        if (!filePath.canRead()) { /* Formato inválido e/ou arquivo corrompido */
            importar = false;
        } else { /* Arquivo valido */

            try {
                /* Limpando banco para que nao haja sobreposição */
                leituraHelper.limparBanco();
                localHelper.limparLocal();
                subLocalHelper.limparSubLocal();
                equipamentoHelper.limparEquipamento();
                statusHelper.limparStatus();
                Log.i(TAG, "lendoTabela: Banco foi limpo! ");

                InputStream inputStream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

                for (int i = 0; i < 5; i++) { /* Passando por todas as tabelas */
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    int rowCount = sheet.getPhysicalNumberOfRows();
                    FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                    for (int r = 1; r < rowCount; r++) { /* Passando por todas as linhas */
                        Row row = sheet.getRow(r);
                        int cellCount = row.getPhysicalNumberOfCells();

                        for (int c = 0; c < cellCount; c++) { /* Passando por todas as colunas */
                            String value = getCellAsString(row, c, formulaEvaluator);

                            switch (i) { /* Inventario(5), EquipamentoInventario(6) e InventarioNegado(7) sobem em branco */
                                case 0: //Leitura
                                    Log.i(TAG, "lendoTabela: Tabela Leitura ");
                                    Leitura l = new Leitura();
                                    switch (c) {
                                        case 0:
                                            double id = Double.valueOf(value);
                                            l.setId((int) id);
                                            break;
                                        case 1:
                                            l.setNumeroTag(value);
                                            break;
                                        case 2:
                                            l.setDataHora(value);
                                            break;
                                        case 3:
                                            double vezesLida = Double.valueOf(value);
                                            l.setVezesLida((int) vezesLida);
                                            break;
                                    }

                                    if (l != null) {
                                        leituraHelper.inserir(l);
                                        Log.i(TAG, "lendoTabela: Tabela Leitura - " + l.getNumeroTag());
                                    }
                                    break;
                                case 1: //Local
                                    Log.i(TAG, "lendoTabela: Tabela Local ");
                                    Local local = new Local();
                                    switch (c) {
                                        case 0:
                                            double id = Double.valueOf(value);
                                            local.setId((int) id);
                                            break;
                                        case 1:
                                            local.setDescricao(value);
                                            break;
                                        default:
                                    }

                                    if (local != null) {
                                        localHelper.inserir(local);
                                        Log.i(TAG, "lendoTabela: Tabela Local - " + local.getDescricao());
                                    }
                                    break;
                                case 2: //SubLocal
                                    Log.i(TAG, "lendoTabela: Tabela SubLocal ");
                                    SubLocal subLocal = new SubLocal();
                                    switch (c) {
                                        case 0:
                                            double id = Double.valueOf(value);
                                            subLocal.setId((int) id);
                                            break;
                                        case 1:
                                           // double idLocal = Double.valueOf(value);
                                            /*subLocal.setIdLocal((int) idLocal);*/
                                            String[] idFinal = value.split(".");
                                            if(idFinal.length > 0){
                                                subLocal.setIdLocal(Integer.parseInt(idFinal[0]));
                                            } else {
                                             double idLocaal = Double.valueOf(value);
                                             if(idLocaal != 0)
                                                 subLocal.setIdLocal((int) idLocaal);
                                            }
                                            break;
                                        case 2:
                                            subLocal.setDescricao(value);
                                            break;
                                        default:
                                    }

                                    if (subLocal != null) {
                                        subLocalHelper.inserir(subLocal);
                                        Log.i(TAG, "lendoTabela: Tabela SubLocal - " + subLocal.getDescricao());
                                    }
                                    break;
                                case 3: //Equipamento
                                    Log.i(TAG, "lendoTabela: Tabela Equipamento ");
                                    Equipamento equipamento = new Equipamento();
                                    switch (c) {
                                        case 0:
                                            double id = Double.valueOf(value);
                                            equipamento.setId((int) id);
                                            break;
                                        case 1:
                                            equipamento.setNumeroTag(value);
                                            break;
                                        case 2:
                                            equipamento.setDescricao(value);
                                            break;
                                        case 3:
                                            double idLocal = Double.valueOf(value);
                                            equipamento.setLocalId((int) idLocal);
                                            break;
                                        case 4:
                                            double idSubLocal = Double.valueOf(value);
                                            equipamento.setSubLocalId((int) idSubLocal);
                                            break;
                                        default:
                                    }

                                    if (equipamento != null) {
                                        equipamentoHelper.inserir(equipamento);
                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getDescricao());
                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getNumeroTag());
                                    }
                                    break;
                                case 4: //Status
                                    Log.i(TAG, "lendoTabela: Tabela Status ");
                                    Status status = new Status();
                                    switch (c) {
                                        case 0:
                                            double id = Double.valueOf(value);
                                            status.setId((int) id);
                                            break;
                                        case 1:
                                            status.setStatus(value);
                                            break;
                                        default:
                                    }

                                    if (status != null) {
                                        statusHelper.inserir(status);
                                        Log.i(TAG, "lendoTabela: Tabela Status - " + status.getStatus());
                                    }
                                    break;
                                default:
                                    break;
                            }

                            String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;
                            Log.i(TAG, "lendoTabela: Dados da celula: " + cellInfo);
                        }
                    }
                }

                importar = true;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "lendoTabela: ERRO: " + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "lendoTabela: ERRO: " + e);
            }
        }

        return importar;
    }

    private String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";

        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    value = "" + cellValue.getBooleanValue();
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    double valorNumerico = cellValue.getNumberValue();
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        double date = cellValue.getNumberValue();
                        SimpleDateFormat formatter =
                                new SimpleDateFormat("MM/dd/yy");
                        value = formatter.format(HSSFDateUtil.getJavaDate(date));
                    } else value = "" + valorNumerico;
                    break;
                case Cell.CELL_TYPE_STRING:
                    value = "" + cellValue.getStringValue();
                    break;
                default:
            }


        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.i(TAG, "lendoTabela: ERRO: " + e);
        }
        return value;
    }
}
