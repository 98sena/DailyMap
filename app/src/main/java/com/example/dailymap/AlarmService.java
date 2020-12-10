package com.example.dailymap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    public AlarmService() {
    }

    NotificationManager Notifi_M;
    ServiceThread thread=null;
    Notification Notifi ;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    String curDG;

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        curDG = intent.getStringExtra("curDG");
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        if(thread==null){
            thread = new ServiceThread(handler,curDG);
            System.out.println("ServiceTest(Service): "+ curDG);
            //thread.setCurDG(curDG);
            thread.start();
        }
        else{
            thread.setCurDG(curDG);
        }
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Intent intent = new Intent(AlarmService.this, Main.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            builder = null;
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //버전 오레오 이상일 경우
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel( new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH) ); ///
                builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
                //하위 버전일 경우
            }
            else{
                builder = new NotificationCompat.Builder(getApplicationContext());
            }


            //알림창 제목
            builder.setContentTitle("Daily Map");

            //알림창 메시지
            builder.setContentText(curDG+"에 다이어리가 추가되었습니다.");

            //알림창 아이콘
            builder.setSmallIcon(R.drawable.ic_pin_dm_green_32);

            //알림창 터치시 상단 알림상태창에서 알림이 자동으로 삭제되게 합니다.
            builder.setAutoCancel(true);

            //head up settings
            builder.setPriority(Notification.PRIORITY_HIGH); //headup

            //pendingIntent를 builder에 설정 해줍니다.
            // 알림창 터치시 인텐트가 전달할 수 있도록 해줍니다.
            //builder.setContentIntent(pendingIntent);
            builder.setFullScreenIntent(pendingIntent,true); //head up settings

            Notification notification = builder.build();

            //알림창 실행
            manager.notify(1,notification);
        }
    };
}
