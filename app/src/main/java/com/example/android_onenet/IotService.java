package com.example.android_onenet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
public class IotService extends Service {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String PRODUCT_ID = "nTH6ND93fp";     /* 产品ID */
    private final String DEVICE_NAME = "kcdz_Device";   /* 设备名称 */
    private final String DEVICE_ACCESSKEY = "MFpYeURnWDFadHAyOUQ2Yzhhd2ZVZVZ2S2J4YmF4eDY=";    /* 设备访问密钥 */
    private String connectToken = "";                   /* 连接Token 注意：获取此Token需要在Token.java文件中修改【用户ID】和【用户密钥】 */
    private final BroadcastReceiver dataReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            /* ***************************************2.2设置值获取 开始******************************** */
            Map<String, Object> properties = new HashMap<>();
            String ledCtrlStr = intent.getStringExtra("Led_Ctrl");
            if(ledCtrlStr != null)
            {
                boolean ledCtrl = Boolean.parseBoolean(ledCtrlStr);
                /* 发送控制信息 */
                properties.put("Led_Ctrl", ledCtrl);
            }
            String fanCtrlStr = intent.getStringExtra("Fan_Ctrl");
            if(fanCtrlStr != null)
            {
                boolean fanCtrl = Boolean.parseBoolean(fanCtrlStr);
                /* 发送控制信息 */
                properties.put("Fan_Ctrl", fanCtrl);
            }
            String temp_limit_upStr = intent.getStringExtra("Temp_Limit_Up");
            if(temp_limit_upStr != null)
            {
                int temp_limit_up = Integer.parseInt(temp_limit_upStr);
                /* 发送控制信息 */
                properties.put("Temp_Limit_Up", temp_limit_up);
            }
            String Humi_Limit_DownStr = intent.getStringExtra("Humi_Limit_Down");
            if(Humi_Limit_DownStr != null)
            {
                int Humi_Limit_Down = Integer.parseInt(Humi_Limit_DownStr);
                /* 发送控制信息 */
                properties.put("Humi_Limit_Down", Humi_Limit_Down);
            }
            String humidifierCtrlStr = intent.getStringExtra("Humidifier_Ctrl");
            if(humidifierCtrlStr != null)
            {
                boolean Humidifier_Ctrl = Boolean.parseBoolean(humidifierCtrlStr);
                /* 发送控制信息 */
                properties.put("Humidifier_Ctrl", Humidifier_Ctrl);
            }
            //.........此处根据项目所需设置
            /* 将控制信息发布至Onenet云平台 */
            publish_message(properties);
            /* ***************************************2.2设置值获取 结束******************************** */
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            connectToken = new Token().key(PRODUCT_ID, DEVICE_NAME, DEVICE_ACCESSKEY);   /* 获取连接Token */

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        registerBroadcastReceiver();            /* 注册广播接收器 */

        handler.post(requestRunnable);          /* 将任务添加至队列，定期执行 */
    }
    /**
     * 注册广播接收器
     */
    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter("com.example.myapplication.DATA_SEND");      //指定你希望接收的广播 com.example.aliyun_mqtt.DATA_RECEIVED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
    }
    /**
     * 请求Onenet数据
     */
    private final Runnable requestRunnable = new Runnable() {
        @Override
        public void run() {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    URL url = new URL("https://iot-api.heclouds.com/thingmodel/query-device-property" +
                            "?product_id=" + PRODUCT_ID + "&device_name=" + DEVICE_NAME);
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("authorization", connectToken);
                    InputStream in = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) result.append(line);
                    reader.close();

                    JSONObject obj = new JSONObject(result.toString());
                    Log.d("IotService", "Request Data: " + obj);
                    JSONArray data = obj.optJSONArray("data");
                    /* 这里定义用于存放物模型数据的临时变量 */
                    Intent intent = new Intent("com.example.myapplication.DATA_RECEIVED");
                    intent.setPackage(getPackageName()); // 限定只发给当前包内的接收器
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject d = data.getJSONObject(i);
                            String id = d.optString("identifier");
                            String value = d.optString("value");
                            long timestamp = d.optLong("time"); // 提取时间戳
                            /* ***************************************1.1获取属性值 开始******************************** */
                            if ("Temp".equals(id))
                            {
                                intent.putExtra("temp_timestamp", timestamp);
                                intent.putExtra("Temp", value);
                            }
                            else if ("Humi".equals(id))
                            {
                                intent.putExtra("humi_timestamp", timestamp);
                                intent.putExtra("Humi", value);
                            }
                            else if ("Led_Ctrl".equals(id))
                            {
                                intent.putExtra("Led_Ctrl", value);
                            }
                            else if ("Fan_Ctrl".equals(id))
                            {
                                intent.putExtra("Fan_Ctrl", value);
                            }
                            else if ("Humidifier_Ctrl".equals(id))
                            {
                                intent.putExtra("Humidifier_Ctrl", value);
                            }
                            //.........根据物模型数据添加
                            /* ***************************************1.1获取属性值 结束******************************** */
                        }
                    }
                    sendBroadcast(intent);
                } catch (Exception e) {
                    Log.e("IotService", "拉取失败", e);
                }
            });
            handler.postDelayed(this, 500);
        }
    };
    /**
     * 向Onenet发布数据
     */
    private void publish_message(Map<String, Object> properties){
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL("https://iot-api.heclouds.com/thingmodel/set-device-desired-property");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("authorization", connectToken);
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                JSONObject params = new JSONObject();
                // 添加所有属性
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    params.put(entry.getKey(), entry.getValue());
                }
                json.put("product_id", PRODUCT_ID);
                json.put("device_name", DEVICE_NAME);
                json.put("params", params);

                Log.d("IotService", "发送控制数据：" + json.toString());

                OutputStream os = conn.getOutputStream(); // 可能出错的地方
                os.write(json.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int code = conn.getResponseCode();

                Log.d("IotService", "控制响应码：" + code);
                // 如果服务器返回错误信息
                InputStream errStream = conn.getErrorStream();
                if (errStream != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(errStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.e("IotService", "错误返回: " + line);
                    }
                }

            } catch (Exception e) {
                Log.e("IotService", "控制发送失败", e);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(requestRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
