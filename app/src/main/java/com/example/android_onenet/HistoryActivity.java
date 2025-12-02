package com.example.android_onenet;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

/**
 * 历史数据Activity：负责显示温湿度历史数据
 */
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";

    // OneNet设备参数
    private final String PRODUCT_ID = "nTH6ND93fp";
    private final String DEVICE_NAME = "kcdz_Device";
    private final String DEVICE_ACCESSKEY = "MFpYeURnWDFadHAyOUQ2Yzhhd2ZVZVZ2S2J4YmF4eDY=";

    private String connectToken = "";

    // UI组件
    private TextView tvTempHistory;
    private TextView tvHumiHistory;
    private ProgressBar progressBar;
    private ImageButton btnRefresh;
    private ImageButton btnBack;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        initToken();
        loadHistoryData();
    }

    /**
     * 初始化UI组件
     */
    private void initViews() {
        tvTempHistory = findViewById(R.id.tv_temp_history);
        tvHumiHistory = findViewById(R.id.tv_humi_history);
        progressBar = findViewById(R.id.progress_bar);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnBack = findViewById(R.id.btn_back);

        // 刷新按钮点击事件
        btnRefresh.setOnClickListener(v -> loadHistoryData());

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * 初始化Token
     */
    private void initToken() {
        try {
            connectToken = new Token().key(PRODUCT_ID, DEVICE_NAME, DEVICE_ACCESSKEY);
        } catch (Exception e) {
            Log.e(TAG, "Token初始化失败", e);
        }
    }

    /**
     * 加载历史数据
     */
    private void loadHistoryData() {
        showLoading(true);
        tvTempHistory.setText("加载中...");
        tvHumiHistory.setText("加载中...");

        // 并行加载温度和湿度历史数据
        loadPropertyHistory("Temp", tvTempHistory, "°C");
        loadPropertyHistory("Humi", tvHumiHistory, "%");
    }

    /**
     * 加载指定属性的历史数据
     * @param identifier 属性标识符 (Temp/Humi)
     * @param textView 显示数据的TextView
     * @param unit 单位
     */
    private void loadPropertyHistory(String identifier, TextView textView, String unit) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 计算时间范围：最近24小时
                long endTime = System.currentTimeMillis();
                long startTime = endTime - 24 * 60 * 60 * 1000; // 24小时前

                // 构建API URL - OneNet物模型历史数据查询API
                String urlStr = "https://iot-api.heclouds.com/thingmodel/query-device-property-history" +
                        "?product_id=" + PRODUCT_ID +
                        "&device_name=" + DEVICE_NAME +
                        "&identifier=" + identifier +
                        "&start=" + startTime +
                        "&end=" + endTime +
                        "&limit=10" +
                        "&sort=desc";

                URL url = new URL(urlStr);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("authorization", connectToken);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "History API Response Code: " + responseCode);

                InputStream inputStream;
                if (responseCode >= 200 && responseCode < 300) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d(TAG, identifier + " History Response: " + result.toString());

                // 解析响应数据
                JSONObject obj = new JSONObject(result.toString());
                int code = obj.optInt("code", -1);

                if (code == 0) {
                    JSONObject data = obj.optJSONObject("data");
                    if (data != null) {
                        JSONArray list = data.optJSONArray("list");
                        if (list != null && list.length() > 0) {
                            StringBuilder historyText = new StringBuilder();
                            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

                            for (int i = 0; i < list.length(); i++) {
                                JSONObject item = list.getJSONObject(i);
                                String value = item.optString("value", "--");
                                long timestamp = item.optLong("time", 0);

                                String timeStr = sdf.format(new Date(timestamp));
                                historyText.append(timeStr)
                                        .append("    ")
                                        .append(value)
                                        .append(unit)
                                        .append("\n");
                            }

                            updateUI(textView, historyText.toString().trim());
                        } else {
                            updateUI(textView, "暂无历史数据");
                        }
                    } else {
                        updateUI(textView, "数据解析失败");
                    }
                } else {
                    String msg = obj.optString("msg", "请求失败");
                    updateUI(textView, "错误: " + msg);
                    Log.e(TAG, identifier + " History Error: " + msg);
                }

            } catch (Exception e) {
                Log.e(TAG, "加载" + identifier + "历史数据失败", e);
                updateUI(textView, "加载失败: " + e.getMessage());
            } finally {
                mainHandler.post(() -> showLoading(false));
            }
        });
    }

    /**
     * 更新UI
     */
    private void updateUI(TextView textView, String text) {
        mainHandler.post(() -> textView.setText(text));
    }

    /**
     * 显示/隐藏加载进度
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
