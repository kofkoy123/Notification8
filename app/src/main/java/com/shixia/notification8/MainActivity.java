package com.shixia.notification8;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

public class MainActivity extends AppCompatActivity {
    private static final int VISIBILITY_SCREEN = -1000;
    private Button mBtnSend;
    private NotificationManager mManager;
    private Button mBtnSendPro;
    private Button mBtnSendCustom;
    private Button mBtnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBtnSend = findViewById(R.id.btn_send_notify);
        mBtnSendPro = findViewById(R.id.btn_send_pro_notify);
        mBtnSendCustom = findViewById(R.id.btn_send_custom_notify);
        mBtnClear = findViewById(R.id.btn_clear_notify);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotify();
            }
        });

        mBtnSendPro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendProNotify();
            }
        });

        mBtnSendCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCustomNotify();
            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.cancelAll();
            }
        });
    }


    /**
     * 发送正常通知
     */
    private void sendNotify() {
        Notification.Builder builder = creatNotification();
        mManager.notify(1, builder.build());
    }

    /**
     * 模拟下载进度条通知
     *
     * @return
     */
    private void sendProNotify() {
        final Notification.Builder builder = creatNotification();
        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);//因为有多次下载回调 会有多次声音所以设置只提示一次
        mManager.notify(2, builder.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
//                        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
                        builder.setProgress(100, i * 10, false);
                        mManager.notify(2, builder.build());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 发送自定义通知栏
     */
    private void sendCustomNotify() {
        Notification.Builder builder = creatNotification();

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.notify_layout);
        remoteView.setTextViewText(R.id.notify_title, "播放音乐通知栏");
        remoteView.setTextViewText(R.id.notify_content, "《昔言》正在播放中....");
        //即将发生的意图可以取消可以更新
        PendingIntent pendingIntent = PendingIntent.getActivity(this, -1,
                new Intent(this, NotifyOpenActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.notify_play, pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setCustomContentView(remoteView);
        }
        mManager.notify(3, builder.build());
    }


    /**
     * 封装一个通知builder
     *
     * @return
     */
    private Notification.Builder creatNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentTitle("消息来了")
                .setContentText("消息通知内容")
                .setSmallIcon(R.mipmap.ic_launcher);
        //适配8.0以上通知栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //第三个参数设置通知的优先级别
            NotificationChannel channel =
                    new NotificationChannel("channel_id", "app_msg", NotificationManager.IMPORTANCE_DEFAULT);
            channel.canBypassDnd();//是否可以绕过请勿打扰模式
            channel.canShowBadge();//是否可以显示icon角标
            channel.enableLights(true);//是否显示通知闪灯
            channel.enableVibration(true);//收到小时时震动提示
            channel.setBypassDnd(true);//设置绕过免打扰
            channel.setLockscreenVisibility(VISIBILITY_SCREEN);
            channel.setLightColor(Color.RED);//设置闪光灯颜色
            channel.getAudioAttributes();//获取设置铃声设置
            channel.setVibrationPattern(new long[]{100, 200, 100});//设置震动模式
            channel.shouldShowLights();//是否会闪光
            mManager.createNotificationChannel(channel);
            builder.setChannelId("channel_id");//这个id参数要与上面channel构建的第一个参数对应
        }
        return builder;
    }


}
