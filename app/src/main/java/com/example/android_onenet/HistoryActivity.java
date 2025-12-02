package com.example.android_onenet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;

/**
 * 历史数据Activity：负责获取和显示Onenet历史数据
 */
public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    // 使用与IotService一致的设备配置
    private final String PRODUCT_ID = "nTH6ND93fp";
    private final String DEVICE_NAME = "kcdz_Device";
    private final String DEVICE_ACCESSKEY = "MFpYeURnWDFadHAyOUQ2Yzhhd2ZVZVZ2S2J4YmF4eDY=";

    // 历史数据查询常量
    private static final long HISTORY_PERIOD_MS = 24 * 60 * 60 * 1000L;  // 查询时间范围：24小时
    private static final int HISTORY_DATA_LIMIT = 50;  // 历史数据查询条数限制

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Spinner spinnerDataType;
    private Button btnQuery;
    private ImageView btnBack;

    private String connectToken = "";
    private String selectedIdentifier = "Temp";

    // 数据类型选项
    private final String[] dataTypeNames = {"温度", "湿度"};
    private final String[] dataTypeIdentifiers = {"Temp", "Humi"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initToken();
        initViews();
        setupSpinner();
        setupListeners();
    }

    /**
     * 初始化Token
     */
    private void initToken() {
        try {
            connectToken = new Token().key(PRODUCT_ID, DEVICE_NAME, DEVICE_ACCESSKEY);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Token初始化失败", e);
        }
    }

    /**
     * 初始化UI组件
     */
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_history);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        spinnerDataType = findViewById(R.id.spinner_data_type);
        btnQuery = findViewById(R.id.btn_query);
        btnBack = findViewById(R.id.btn_back);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);
    }

    /**
     * 设置Spinner
     */
    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dataTypeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataType.setAdapter(spinnerAdapter);

        spinnerDataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedIdentifier = dataTypeIdentifiers[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedIdentifier = dataTypeIdentifiers[0];
            }
        });
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnQuery.setOnClickListener(v -> fetchHistoryData());
    }

    /**
     * 获取历史数据
     */
    private void fetchHistoryData() {
        showLoading(true);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 计算时间范围
                long endTime = System.currentTimeMillis();
                long startTime = endTime - HISTORY_PERIOD_MS;

                // 构建URL - 使用Onenet历史数据查询API
                String urlStr = "https://iot-api.heclouds.com/thingmodel/query-device-property-history" +
                        "?product_id=" + PRODUCT_ID +
                        "&device_name=" + DEVICE_NAME +
                        "&identifier=" + selectedIdentifier +
                        "&start=" + startTime +
                        "&end=" + endTime +
                        "&limit=" + HISTORY_DATA_LIMIT +
                        "&sort=desc";

                URL url = new URL(urlStr);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("authorization", connectToken);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                Log.d(TAG, "Response: " + result.toString());

                // 解析数据
                List<HistoryDataItem> historyList = parseHistoryData(result.toString());

                // 更新UI
                runOnUiThread(() -> {
                    showLoading(false);
                    if (historyList.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                        adapter.setDataList(historyList);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "获取历史数据失败", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    showEmpty(true);
                    tvEmpty.setText("获取数据失败\n请检查网络连接");
                });
            }
        });
    }

    /**
     * 解析历史数据JSON
     */
    private List<HistoryDataItem> parseHistoryData(String jsonStr) {
        List<HistoryDataItem> list = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(jsonStr);
            JSONObject data = obj.optJSONObject("data");
            if (data != null) {
                JSONArray datapoints = data.optJSONArray("datapoints");
                if (datapoints != null) {
                    for (int i = 0; i < datapoints.length(); i++) {
                        JSONObject dp = datapoints.getJSONObject(i);
                        String value = dp.optString("value");
                        long timestamp = dp.optLong("at");
                        list.add(new HistoryDataItem(selectedIdentifier, value, timestamp));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析数据失败", e);
        }
        return list;
    }

    /**
     * 显示/隐藏加载状态
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * 显示/隐藏空数据提示
     */
    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            tvEmpty.setText("暂无历史数据\n请点击查询按钮获取数据");
        }
    }
}
