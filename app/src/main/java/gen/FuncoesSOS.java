package gen;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import android.util.Log;


import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.rfidscanner.R;

import telas.HomeActivity;
import timber.log.Timber;

public class FuncoesSOS {
    public static final int NOTIFICATION_ID_PADRAO = 10;
    public static final String NOTIFICATION_CHANNEL_ID_PADRAO = "NOTIFICATION_CHANNEL_NOTIFICATION_CHANNEL_ID_PADRAO";
    public static NotificationCompat.Builder notificationBuilder;

    public static final int NOTIFICATION_ID_NOVA_MENSAGEM = 14;
    public static final String NOTIFICATION_CHANNEL_ID_NOVA_MENSAGEM = "NOTIFICATION_CHANNEL_ID_NOVA_MENSAGEM";

    public static final int NOTIFICATION_ID_CORRIDA_CANCELAMENTO = 15;
    public static final String NOTIFICATION_CHANNEL_ID_CORRIDA_CANCELAMENTO = "NOTIFICATION_CHANNEL_ID_CORRIDA_CANCELAMENTO";


    // Notificação padrão que será usada em todos os serviços, exceto no Servico Chat
    public static Notification sendNotificationPadrao(Context context) {
        Timber.i("sendNotificationPadrao = ");

        String titulo = "SOS";
        String texto = "App para facilitar seu trabalho";
        String textoBig = "";

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int color = ContextCompat.getColor(context, R.color.colorPrimary);

        // if (notificationBuilder == null) {
        notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_PADRAO);
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_sos)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setAutoCancel(false)
                .setTicker(titulo)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setColor(color)
                .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(texto).bigText(textoBig))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
       /* } else {
            notificationBuilder
                    .setContentTitle(titulo)
                    .setContentText(texto)
                    .setTicker(titulo);
        }*/

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_PADRAO, "SOS", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(color);
            assert notificationManager != null;
            notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID_PADRAO);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID_PADRAO, notificationBuilder.build());
        return notificationBuilder.build();
    }
}