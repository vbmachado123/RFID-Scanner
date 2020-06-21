package util;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import dao.EquipamentoDao;
import dao.EquipamentoInventarioDao;
import dao.InventarioDao;
import dao.InventarioNegadoDao;
import dao.LeituraDao;
import dao.LocalDao;
import dao.StatusDao;
import dao.SubLocalDao;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import model.Equipamento;
import model.EquipamentoInventario;
import model.Inventario;
import model.InventarioNegado;
import model.Leitura;
import model.Local;
import model.Status;
import model.SubLocal;

public class Xlsx {

    private static final String TAG = "Importacao";
    private Context context;

    /* BANCO DE DADOS */
    private EquipamentoDao equipamentoDao;
    private EquipamentoInventarioDao equipamentoInventarioDao;
    private InventarioDao inventarioDao;
    private InventarioNegadoDao inventarioNegadoDao;
    private LeituraDao leituraDao;
    private LocalDao localDao;
    private StatusDao statusDao;
    private SubLocalDao subLocalDao;

    /* Models */
    private Leitura leitura = new Leitura();
    private Local local = new Local();
    private SubLocal subLocal = new SubLocal();
    private Equipamento equipamento = new Equipamento();
    private Status status = new Status();

    public Xlsx(Context context) {
        this.context = context;

        leituraDao = new LeituraDao(context);
        localDao = new LocalDao(context);
        subLocalDao = new SubLocalDao(context);
        equipamentoDao = new EquipamentoDao(context);
        statusDao = new StatusDao(context);
        equipamentoInventarioDao = new EquipamentoInventarioDao(context);
        inventarioDao = new InventarioDao(context);
        inventarioNegadoDao = new InventarioNegadoDao(context);
    }

    /* Método responsável por importar a tabela - Converter e Salvar no banco*/
    public boolean importarTabela(File filePath) {
        boolean importar = false;
        Log.i("Importacao", "Importar foi inciado " + filePath);
        if (filePath.getPath().isEmpty()) { /* Formato inválido e/ou arquivo corrompido */
            importar = false;
        } else { /* Arquivo valido */
            try {
                /* Limpando banco para que nao haja sobreposição */
                leituraDao.limparTabela();
                localDao.limparTabela();
                subLocalDao.limparTabela();
                equipamentoDao.limparTabela();
                statusDao.limparTabela();
                equipamentoInventarioDao.limparTabela();
                inventarioDao.limparTabela();
                inventarioNegadoDao.limparTabela();

                Log.i(TAG, "lendoTabela: Banco foi limpo! ");

                InputStream inputStream = new FileInputStream(filePath);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

                if (workbook != null) {
                    for (int i = 0; i < 5; i++) { /* Passando por todas as tabelas */

                        XSSFSheet sheet = workbook.getSheetAt(i);
                        int rowCount = sheet.getPhysicalNumberOfRows();
                        FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

                        for (int r = 1; r < rowCount; r++) { /* Passando por todas as linhas */
                            Row row = sheet.getRow(r);
                            try {
                                int cellCount = row.getPhysicalNumberOfCells();
                                for (int c = 0; c < cellCount; c++) { /* Passando por todas as colunas */
                                    if (row.getPhysicalNumberOfCells() < 0)
                                        break; /* Tabela chegou ao fim, ir para a próxima */
                                    else { /* Ainda tem conteúdo */
                                        String value = getCellAsString(row, c, formulaEvaluator);
                                        switch (i) { /* Inventario(5), EquipamentoInventario(6) e InventarioNegado(7) sobem em branco */
                                            case 0: /* Leitura */
                                                switch (c) { /* Verifica qual a coluna */
                                                    case 0:
                                                        Log.i(TAG, "lendoTabela: Tabela Leitura ");
                                                        double id = Double.valueOf(value);
                                                        leitura.setId((int) id);
                                                        break;
                                                    case 1:
                                                        leitura.setNumeroTag(value);
                                                        break;
                                                    case 2:
                                                        leitura.setDataHora(value);
                                                        break;
                                                    case 3:
                                                        double vezesLida = Double.valueOf(value);
                                                        leitura.setVezesLida((int) vezesLida);
                                                        leituraDao.inserir(leitura);
                                                        Log.i(TAG, "lendoTabela: Tabela Leitura - " + leitura.getId());
                                                        Log.i(TAG, "lendoTabela: Tabela Leitura - " + leitura.getNumeroTag());
                                                        Log.i(TAG, "lendoTabela: Tabela Leitura - " + leitura.getDataHora());
                                                        Log.i(TAG, "lendoTabela: Tabela Leitura - " + leitura.getVezesLida());
                                                        break;
                                                }

                                                break;
                                            case 1: /* Local */
                                                switch (c) {
                                                    case 0:
                                                        Log.i(TAG, "lendoTabela: Tabela Local ");
                                                        double id = Double.valueOf(value);
                                                        local.setId((int) id);
                                                        break;
                                                    case 1:
                                                        local.setDescricao(value);
                                                        localDao.inserir(local);
                                                        Log.i(TAG, "lendoTabela: Tabela Local - " + local.getId());
                                                        Log.i(TAG, "lendoTabela: Tabela Local - " + local.getDescricao());
                                                        break;
                                                    default:
                                                }
                                                break;
                                            case 2: /* SubLocal */
                                                switch (c) {
                                                    case 0:
                                                        Log.i(TAG, "lendoTabela: Tabela SubLocal ");
                                                        double id = Double.valueOf(value);
                                                        subLocal.setId((int) id);
                                                        break;
                                                    case 1:
                                                        // double idLocal = Double.valueOf(value);
                                                        /*subLocal.setIdLocal((int) idLocal);*/
                                                        String[] idFinal = value.split(".");
                                                        if (idFinal.length > 0) {
                                                            subLocal.setIdLocal(Integer.parseInt(idFinal[0]));
                                                        } else {
                                                            double idLocaal = Double.valueOf(value);
                                                            if (idLocaal != 0)
                                                                subLocal.setIdLocal((int) idLocaal);
                                                        }
                                                        break;
                                                    case 2:
                                                        subLocal.setDescricao(value);
                                                        subLocalDao.inserir(subLocal);
                                                        Log.i(TAG, "lendoTabela: Tabela SubLocal - " + subLocal.getDescricao());
                                                        break;
                                                    default:
                                                }
                                                break;
                                            case 3: /* Equipamento */
                                                switch (c) {
                                                    case 0:
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento ");
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
                                                        equipamentoDao.inserir(equipamento);
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getId());
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getNumeroTag());
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getDescricao());
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getLocalId());
                                                        Log.i(TAG, "lendoTabela: Tabela Equipamento - " + equipamento.getSubLocalId());
                                                        break;
                                                    default:
                                                }

                                                break;
                                            case 4: /* Status */
                                                switch (c) {
                                                    case 0:
                                                        Log.i(TAG, "lendoTabela: Tabela Status ");
                                                        double id = Double.valueOf(value);
                                                        status.setId((int) id);
                                                        break;
                                                    case 1:
                                                        status.setStatus(value);
                                                        statusDao.inserir(status);
                                                        Log.i(TAG, "lendoTabela: Tabela Status - " + status.getId());
                                                        Log.i(TAG, "lendoTabela: Tabela Status - " + status.getStatus());
                                                        break;
                                                    default:
                                                }
                                                break;
                                            default:
                                                break;
                                        }
                                        String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;
                                        // Log.i(TAG, "lendoTabela: Dados da celula: " + cellInfo);
                                    }
                                }
                            } catch (NullPointerException e) {
                                /* Célula vazia */
                                e.printStackTrace();
                            }
                        }
                    }

                    importar = true;

                } else Log.i(TAG, "lendoTabela: A tabela não possui dados!!!!!! ");

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

    /* Método responsável por capturar e converter o valor da célula */
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

    /* Método responsável por Converter, Salvar, Exportar e limpar o banco */
    public boolean exportarTabela(Context context) {
        boolean exporta = false;

        /* Recuperando dados */
        EquipamentoDao equipamentoDao = new EquipamentoDao(context);
        EquipamentoInventarioDao equipamentoInventarioDao = new EquipamentoInventarioDao(context);
        InventarioDao inventarioDao = new InventarioDao(context);
        InventarioNegadoDao inventarioNegadoDao = new InventarioNegadoDao(context);
        LeituraDao leituraDao = new LeituraDao(context);
        LocalDao localDao = new LocalDao(context);
        StatusDao statusDao = new StatusDao(context);
        SubLocalDao subLocalDao = new SubLocalDao(context);

        String dataFinal = Data.getDataEHoraAual("ddMMyyyy_HHmm");

        File exportDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SOSRFiD");
        if (!exportDir.exists()) exportDir.mkdirs();

        Inventario inventario = inventarioDao.recupera();
        Local local = localDao.getById(inventario.getIdLocal());
        SubLocal subLocal = subLocalDao.getById(inventario.getIdSubLocal());


        File file = new File(exportDir, "Inventario - " + local.getDescricao() + " - " + subLocal.getDescricao() + ".xls");
        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("pt", "BR"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("Atribuidos", 0);

            /* CABEÇALHO */
            sheet.addCell(new Label(0, 0, String.valueOf(inventario.getId())));
            sheet.addCell(new Label(1, 0, local.getDescricao()));
            /*sheet.addCell(new Label(2, 0, subLocal.getDescricao()));*/
            sheet.addCell(new Label(2, 0, inventario.getDataHora()));
            sheet.addCell(new Label(3, 0, inventario.getLatitude()));
            sheet.addCell(new Label(4, 0, inventario.getLongitude()));
            sheet.addCell(new Label(5, 0, inventario.getEndereco()));

            /* EquipamentoInventario */
            ArrayList<EquipamentoInventario> eiList = (ArrayList<EquipamentoInventario>) equipamentoInventarioDao.obterTodos();

            for (int r = 2; r < eiList.size() + 2; r++) { /* Passando por todos os itens da lista */
                int i = r -2;
                for (int c = 0; c < 6; c++) { /* Passando por todas as colunas */
                    EquipamentoInventario ei = eiList.get(i);
                    Equipamento e = equipamentoDao.getById(ei.getIdEquipamento());
                    switch (c) { /* Preenchendo as células */
                  /*      case 0:; Preenche o numero do inventario na 1 coluna
                            sheet.addCell(new Label(c, r, String.valueOf(ei.getIdInventario())));
                            Log.i("Exportacao", "Celula: " + c + r + String.valueOf(ei.getIdInventario()));
                            break;*/
                        case 1:
                            sheet.addCell(new Label(c, r, e.getNumeroTag()));
                            Log.i("Exportacao", "Celula: " + c + r + e.getNumeroTag());
                            break;
                        case 2:
                            sheet.addCell(new Label(c, r, e.getDescricao()));
                            Log.i("Exportacao", "Celula: " + c + r + e.getDescricao());
                            break;
                        case 3:
                            sheet.addCell(new Label(c, r, ei.getLatitude()));
                            Log.i("Exportacao", "Celula: " + c + r + ei.getLatitude());
                            break;
                        case 4:
                            sheet.addCell(new Label(c, r, ei.getLongitude()));
                            Log.i("Exportacao", "Celula: " + c + r + ei.getLongitude());
                            break;
                        case 5:
                            sheet.addCell(new Label(c, r, subLocal.getDescricao()));
                            break;
                    }
                }
            }

            InventarioNegado inventarioNegado = inventarioNegadoDao.recupera();
            if(inventarioNegado != null){
                WritableSheet sheet1 = workbook.createSheet("Nao_Atribuidos", 1);

                ArrayList<InventarioNegado> inventarioNegadoList = (ArrayList<InventarioNegado>) inventarioNegadoDao.obterTodos();
                Cursor cursor = inventarioNegadoDao.pegaCursor();
                if(cursor.moveToFirst()){
                    sheet1.addCell(new Label(0, 0, "id"));
                    sheet1.addCell(new Label(1, 0, "idInventario"));
                    sheet1.addCell(new Label(2, 0, "numeroTag"));
                    sheet1.addCell(new Label(3, 0, "dataHora"));
                    sheet1.addCell(new Label(4, 0, "latitude"));
                    sheet1.addCell(new Label(5, 0, "longitude"));

                    do{
                        String id = String.valueOf(cursor.getInt(cursor.getColumnIndex("id")));
                        String idInventario = String.valueOf(cursor.getInt(cursor.getColumnIndex("idInventario")));
                        String numeroTag = cursor.getString(cursor.getColumnIndex("numeroTag"));
                        String dataHora = cursor.getString(cursor.getColumnIndex("dataHora"));
                        String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                        String longitude = cursor.getString(cursor.getColumnIndex("longitude"));

                        int i = cursor.getPosition() + 1;
                        sheet1.addCell(new Label(0, i, id));
                        sheet1.addCell(new Label(1, i, idInventario));
                        sheet1.addCell(new Label(2, i, numeroTag));
                        sheet1.addCell(new Label(3, i, dataHora));
                        sheet1.addCell(new Label(4, i, latitude));
                        sheet1.addCell(new Label(5, i, longitude));

                    }while (cursor.moveToNext());
                }

            }

            workbook.write();
            workbook.close();
            exporta = true;

            equipamentoDao.limparTabela();
            equipamentoInventarioDao.limparTabela();
            inventarioDao.limparTabela();
            inventarioNegadoDao.limparTabela();
            leituraDao.limparTabela();
            localDao.limparTabela();
            statusDao.limparTabela();
            subLocalDao.limparTabela();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Exportacao", "Erro: " + e);
        }

        return exporta;
    }
}
