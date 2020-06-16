package util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Data {
    private Context context;

    public Data(Context context) {
        this.context = context;
    }

    public static String getDataEHoraAual(String formato){
         SimpleDateFormat dataFormatada;
         Date date;
         String dataFinal = "";

        dataFormatada = new SimpleDateFormat(formato);
        date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Date dataAtual = calendar.getTime();
        dataFinal = dataFormatada.format(dataAtual);

        return dataFinal;
    }
}
