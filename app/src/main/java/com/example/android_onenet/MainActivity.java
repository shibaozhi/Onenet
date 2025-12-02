package com.example.android_onenet;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
/**
 * 主Activity：负责界面显示与用户交互
 */
public class MainActivity extends AppCompatActivity {

    // UI组件
    private TextView humiTextView;
    private TextView tempTextView;
    private TextView humiTimeTextView;
    private TextView tempTimeTextView;

    private SeekBar humiLimitDownSeekBar;
    private TextView humiLimitDown_tv;
    private SeekBar tempLimitUpSeekBar;
    private TextView tempLimitUp_tv;
    int temp_limit_up;
    int humi_limit_down;
    private Button Para_Save_Btn;   //参数设置保存按钮
    private boolean ledCtrl = false;
    /* 设备状态相关变量 */
    private boolean device_status = false;  //当前设备状态
    private String  device_data_preupdate_time = "";    //设备上次数据更新时间
    private String  device_data_nowupdate_time = "";    //设备本次数据更新时间
    private boolean device_status_detection = false;    //是否开启设备状态检测
    private int device_offline_detection = 0;           //离线检测次数
    /* ***************************************2.1控制命令下发 开始******************************** */
    /* 灯光控制事件 (由Switch触发) */
    private boolean ledWait = false;    //等待对比上传值与设置值
    private int ledSameCount = 0;       //相同次数
    private int ledCurrentCount = 0;    //近期上传次数
    private boolean ledPreState = false;
    public void toggleLed(View view) {
        SwitchMaterial ledSwitch1 = (SwitchMaterial) view;
        ledSwitch1.setEnabled(false); // 立即禁用开关
        // 500ms后重新启用开关
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ledSwitch1.setEnabled(true);
        }, 500);

        SwitchMaterial ledSwitch = (SwitchMaterial) view;
        if(device_status != true){
            View rootView = findViewById(android.R.id.content); // 或任意父布局
            Snackbar.make(rootView,
                    "设备不在线，操作失败",
                    Snackbar.LENGTH_SHORT).show();
            ledSwitch.setChecked(ledCtrl);
            return ;
        }
        ImageView ledIcon = findViewById(R.id.led_icon_style1);
        MaterialCardView ledCard = findViewById(R.id.led_card);
//        private boolean ledCtrl = false;
        ledCtrl = ledSwitch.isChecked(); // 直接获取开关状态
        ledWait = true; /* 等待检查上传值 */
        ledCurrentCount = 0;
        ledSameCount = 0;
        if (ledCtrl) {
            ledIcon.setImageResource(R.drawable.ic_led_on);
            ledCard.setStrokeColor(Color.parseColor("#2196F3"));
        } else {
            ledIcon.setImageResource(R.drawable.ic_led_off);
            ledCard.setStrokeColor(Color.parseColor("#CAC4D0"));
        }

        Intent intent = new Intent("com.example.myapplication.DATA_SEND");
        intent.setPackage(getPackageName());
        intent.putExtra("Led_Ctrl", String.valueOf(ledCtrl));
        sendBroadcast(intent);
    }
    /* 加湿器控制事件 (由Switch触发) */
    private boolean humidifierCtrl = false;
    private boolean humidifierWait = false;    //等待对比上传值与设置值
    private int humidifierSameCount = 0;       //相同次数
    private int humidifierCurrentCount = 0;    //近期上传次数
    private boolean humidifierPreState = false;
    public void toggleHumidifier(View view) {
        SwitchMaterial humidifierSwitch1 = (SwitchMaterial) view;
        humidifierSwitch1.setEnabled(false); // 立即禁用开关
        // 500ms后重新启用开关
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            humidifierSwitch1.setEnabled(true);
        }, 500);

        SwitchMaterial humidifierSwitch = (SwitchMaterial) view;
        if(device_status != true){
            View rootView = findViewById(android.R.id.content); // 或任意父布局
            Snackbar.make(rootView,
                    "设备不在线，操作失败",
                    Snackbar.LENGTH_SHORT).show();
            humidifierSwitch.setChecked(humidifierCtrl);
            return ;
        }
        ImageView humidifierIcon = findViewById(R.id.humidifier_icon_style1);
        MaterialCardView humidifierCard = findViewById(R.id.humidifier_card);
        humidifierCtrl = humidifierSwitch.isChecked(); // 直接获取开关状态
        humidifierWait = true; /* 等待检查上传值 */
        humidifierCurrentCount = 0;
        humidifierSameCount = 0;
        if (humidifierCtrl) {
            humidifierIcon.setImageResource(R.drawable.ic_humidifier_on);
            humidifierCard.setStrokeColor(Color.parseColor("#2196F3"));
        } else {
            humidifierIcon.setImageResource(R.drawable.ic_humidifier_off);
            humidifierCard.setStrokeColor(Color.parseColor("#CAC4D0"));
        }

        Intent intent = new Intent("com.example.myapplication.DATA_SEND");
        intent.setPackage(getPackageName());
        intent.putExtra("Humidifier_Ctrl", String.valueOf(humidifierCtrl));
        sendBroadcast(intent);
    }
    /* 风扇控制事件 (由Switch触发) */
    private boolean fanCtrl = false;
    private boolean fanWait = false;    //等待对比上传值与设置值
    private int fanSameCount = 0;       //相同次数
    private int fanCurrentCount = 0;    //近期上传次数
    private boolean fanPreState = false;
    public void toggleFan(View view) {
        SwitchMaterial fanSwitch1 = (SwitchMaterial) view;
        fanSwitch1.setEnabled(false); // 立即禁用开关
        // 500ms后重新启用开关
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            fanSwitch1.setEnabled(true);
        }, 500);
        SwitchMaterial fanSwitch = (SwitchMaterial) view;
        if(device_status != true){
            View rootView = findViewById(android.R.id.content); // 或任意父布局
            Snackbar.make(rootView,
                    "设备不在线，操作失败",
                    Snackbar.LENGTH_SHORT).show();
            fanSwitch.setChecked(fanCtrl);
            return ;
        }

        ImageView fanIcon = findViewById(R.id.fan_icon_style1);
        MaterialCardView fanCard = findViewById(R.id.fan_card);

        fanCtrl = fanSwitch.isChecked(); // 直接获取开关状态
        fanWait = true; /* 等待检查上传值 */
        fanCurrentCount = 0;
        fanSameCount = 0;
        if (fanCtrl) {
            fanIcon.setImageResource(R.drawable.ic_fan_on);
            fanCard.setStrokeColor(Color.parseColor("#2196F3"));
        } else {
            fanIcon.setImageResource(R.drawable.ic_fan_off);
            fanCard.setStrokeColor(Color.parseColor("#CAC4D0"));
        }

        // --- 同样发送广播来控制硬件 ---
        Intent intent = new Intent("com.example.myapplication.DATA_SEND");
        intent.setPackage(getPackageName());
        intent.putExtra("Fan_Ctrl", String.valueOf(fanCtrl));
        sendBroadcast(intent);
    }
    /* ***************************************2.1控制命令下发 结束******************************** */

    private final BroadcastReceiver dataReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String Text = "";
            /* ***********************************1.2设置属性值 开始******************************************* */
            // 获取属性值
            String temp = intent.getStringExtra("Temp");
            Long temp_timestamp = intent.getLongExtra("temp_timestamp",0L);
            String humi = intent.getStringExtra("Humi");
            Long humi_timestamp = intent.getLongExtra("humi_timestamp",0L);
            String Led_Ctrl_Str = intent.getStringExtra("Led_Ctrl");
            String Fan_Ctrl_Str = intent.getStringExtra("Fan_Ctrl");
            String Humidifier_Ctrl_Str = intent.getStringExtra("Humidifier_Ctrl");

            // 更新UI
            /* 处理温度数据 */
            if(temp != null && !temp.isEmpty()){
                Text = temp + "°C";
                Date date = new Date(temp_timestamp); // 创建Date对象
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); // 定义日期格式
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置为东八区（北京时间）
                String formattedDate = sdf.format(date); // 格式化为可读字符串

                /* 设备状态检测 - 开始 */
                if(device_status_detection == false) {
                    device_status_detection = true;
                    device_data_nowupdate_time = formattedDate;
                }
                else{
                    device_data_nowupdate_time = formattedDate;
                    if(!device_data_preupdate_time.equals(device_data_nowupdate_time))
                    {
                        /* 标记设备在线 */
                        device_offline_detection = 0;
                        device_status = true;
                    }
                    else{
                        /* 记录离线次数 */
                        device_offline_detection+=1;
                        if(device_offline_detection > 20)
                        {
                            device_status = false;
                        }
                    }
                    ImageView deviceIcon = findViewById(R.id.device_icon_style1);
                    MaterialCardView deviceCard = findViewById(R.id.device_card);
                    TextView device_tv = findViewById(R.id.device_text_Status);
                    if (device_status) {
                        device_tv.setTextColor(Color.parseColor("#2196F3"));
                        device_tv.setText("在线");
                        deviceIcon.setImageResource(R.drawable.ic_device_online);
                        deviceCard.setStrokeColor(Color.parseColor("#2196F3"));
                    } else {
                        device_tv.setText("离线");
                        device_tv.setTextColor(Color.parseColor("#FF3B2F"));
                        deviceIcon.setImageResource(R.drawable.ic_device_offline);
                        deviceCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                    }
                }
                device_data_preupdate_time = device_data_nowupdate_time;
                /* 设备状态检测 - 结束 */

                tempTimeTextView.setText(formattedDate);
                tempTextView.setText(Text);
            }
            /* 处理湿度数据 */
            if(humi!= null && !humi.isEmpty()) {
                Text = humi + "%";
                humiTextView.setText(Text);
                Date date = new Date(humi_timestamp); // 创建Date对象
                //SimpleDateFormat sdf = new SimpleDateFormat("yyyy\r\nMM-dd\r\nHH:mm:ss"); // 定义日期格式
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+8")); // 设置为东八区（北京时间）
                String formattedDate = sdf.format(date); // 格式化为可读字符串
                humiTimeTextView.setText(formattedDate);
            }
            /* 灯光控制执行结果判断 */
            if (Led_Ctrl_Str !=null && !Led_Ctrl_Str.isEmpty()) {
                if(ledWait == true)
                {
                    ledCurrentCount++;
                    if ((ledCtrl == Boolean.parseBoolean(Led_Ctrl_Str))) {
                        ledSameCount++;
                        if(ledSameCount >= 1)
                        {
                            ledSameCount = 0;
                            ledWait = false;
                            ledCurrentCount = 0;
                            View rootView = findViewById(android.R.id.content); // 或任意父布局
                            Snackbar.make(rootView,
                                            "灯光已" + (ledCtrl ? "开启" : "关闭"),
                                            Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    if (ledCurrentCount >= 12) {
                        ledSameCount = 0;
                        ledWait = false;
                        ledCurrentCount = 0;
                        View rootView = findViewById(android.R.id.content); // 或任意父布局
                        Snackbar.make(rootView,
                                "灯光操作失败",
                                Snackbar.LENGTH_SHORT).show();
                        //led_switch
                        SwitchMaterial led_switch = findViewById(R.id.led_switch);
                        ledCtrl = Boolean.parseBoolean(Led_Ctrl_Str);//同步状态
                        led_switch.setChecked(ledCtrl);
                        ImageView ledIcon = findViewById(R.id.led_icon_style1);
                        MaterialCardView ledCard = findViewById(R.id.led_card);
                        if (ledCtrl) {
                            ledIcon.setImageResource(R.drawable.ic_led_on);
                            ledCard.setStrokeColor(Color.parseColor("#2196F3"));
                        } else {
                            ledIcon.setImageResource(R.drawable.ic_led_off);
                            ledCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                        }
                    }
                }
                else{
                    if(ledPreState == Boolean.parseBoolean(Led_Ctrl_Str)) {
                        ledSameCount++;
                        if(ledSameCount >= 5)
                        {
                            SwitchMaterial led_switch = findViewById(R.id.led_switch);
                            ledCtrl = Boolean.parseBoolean(Led_Ctrl_Str);//同步状态
                            led_switch.setChecked(ledCtrl);
                            ImageView ledIcon = findViewById(R.id.led_icon_style1);
                            MaterialCardView ledCard = findViewById(R.id.led_card);
                            if (ledCtrl) {
                                ledIcon.setImageResource(R.drawable.ic_led_on);
                                ledCard.setStrokeColor(Color.parseColor("#2196F3"));
                            } else {
                                ledIcon.setImageResource(R.drawable.ic_led_off);
                                ledCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                            }
                        }
                    }
                    else {
                        ledSameCount = 0;
                    }
                    ledPreState = Boolean.parseBoolean(Led_Ctrl_Str);

                }
            }
            /* 风扇控制执行结果判断 */
            if (Fan_Ctrl_Str != null && !Fan_Ctrl_Str.isEmpty()) {
                if(fanWait == true)
                {
                    fanCurrentCount++;
                    if ((fanCtrl == Boolean.parseBoolean(Fan_Ctrl_Str))) {
                        fanSameCount++;
                        if(fanSameCount >= 1)
                        {
                            fanSameCount = 0;
                            fanWait = false;
                            fanCurrentCount = 0;
                            View rootView = findViewById(android.R.id.content); // 或任意父布局
                            Snackbar.make(rootView,
                                            "风扇已" + (fanCtrl ? "开启" : "关闭"),
                                            Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    if (fanCurrentCount >= 12) {
                        fanSameCount = 0;
                        fanWait = false;
                        fanCurrentCount = 0;
                        View rootView = findViewById(android.R.id.content); // 或任意父布局
                        Snackbar.make(rootView,
                                "风扇操作失败",
                                Snackbar.LENGTH_SHORT).show();
                        //fan_switch
                        SwitchMaterial fan_switch = findViewById(R.id.fan_switch);
                        fanCtrl = Boolean.parseBoolean(Fan_Ctrl_Str);//同步状态
                        fan_switch.setChecked(fanCtrl);
                        ImageView fanIcon = findViewById(R.id.fan_icon_style1);
                        MaterialCardView fanCard = findViewById(R.id.fan_card);
                        if (fanCtrl) {
                            fanIcon.setImageResource(R.drawable.ic_fan_on);
                            fanCard.setStrokeColor(Color.parseColor("#2196F3"));
                        } else {
                            fanIcon.setImageResource(R.drawable.ic_fan_off);
                            fanCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                        }
                    }
                }
                else{
                    if(fanPreState == Boolean.parseBoolean(Fan_Ctrl_Str)) {
                        fanSameCount++;
                        if(fanSameCount >= 5)
                        {
                            SwitchMaterial fan_switch = findViewById(R.id.fan_switch);
                            fanCtrl = Boolean.parseBoolean(Fan_Ctrl_Str);//同步状态
                            fan_switch.setChecked(fanCtrl);
                            ImageView fanIcon = findViewById(R.id.fan_icon_style1);
                            MaterialCardView fanCard = findViewById(R.id.fan_card);
                            if (fanCtrl) {
                                fanIcon.setImageResource(R.drawable.ic_fan_on);
                                fanCard.setStrokeColor(Color.parseColor("#2196F3"));
                            } else {
                                fanIcon.setImageResource(R.drawable.ic_fan_off);
                                fanCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                            }
                        }
                    }
                    else {
                        fanSameCount = 0;
                    }
                    fanPreState = Boolean.parseBoolean(Fan_Ctrl_Str);

                }
            }
            /* 加湿器控制执行结果判断 */
            if (Humidifier_Ctrl_Str != null && !Humidifier_Ctrl_Str.isEmpty()) {
                if(humidifierWait == true)
                {
                    humidifierCurrentCount++;
                    if ((humidifierCtrl == Boolean.parseBoolean(Humidifier_Ctrl_Str))) {
                        humidifierSameCount++;
                        if(humidifierSameCount >= 1)
                        {
                            humidifierSameCount = 0;
                            humidifierWait = false;
                            humidifierCurrentCount = 0;
                            View rootView = findViewById(android.R.id.content); // 或任意父布局
                            Snackbar.make(rootView,
                                            "加湿器已" + (humidifierCtrl ? "开启" : "关闭"),
                                            Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    }
                    if (humidifierCurrentCount >= 12) {
                        humidifierSameCount = 0;
                        humidifierWait = false;
                        humidifierCurrentCount = 0;
                        View rootView = findViewById(android.R.id.content); // 或任意父布局
                        Snackbar.make(rootView,
                                "加湿器操作失败",
                                Snackbar.LENGTH_SHORT).show();
                        //humidifier_switch
                        SwitchMaterial humidifier_switch = findViewById(R.id.humidifier_switch);
                        humidifierCtrl = Boolean.parseBoolean(Humidifier_Ctrl_Str);//同步状态
                        humidifier_switch.setChecked(humidifierCtrl);
                        ImageView humidifierIcon = findViewById(R.id.humidifier_icon_style1);
                        MaterialCardView humidifierCard = findViewById(R.id.humidifier_card);
                        if (humidifierCtrl) {
                            humidifierIcon.setImageResource(R.drawable.ic_humidifier_on);
                            humidifierCard.setStrokeColor(Color.parseColor("#2196F3"));
                        } else {
                            humidifierIcon.setImageResource(R.drawable.ic_humidifier_off);
                            humidifierCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                        }
                    }
                }
                else{
                    if(humidifierPreState == Boolean.parseBoolean(Humidifier_Ctrl_Str)) {
                        humidifierSameCount++;
                        if(humidifierSameCount >= 5)
                        {
                            SwitchMaterial humidifier_switch = findViewById(R.id.humidifier_switch);
                            humidifierCtrl = Boolean.parseBoolean(Humidifier_Ctrl_Str);//同步状态
                            humidifier_switch.setChecked(humidifierCtrl);
                            ImageView humidifierIcon = findViewById(R.id.humidifier_icon_style1);
                            MaterialCardView humidifierCard = findViewById(R.id.humidifier_card);
                            if (humidifierCtrl) {
                                humidifierIcon.setImageResource(R.drawable.ic_humidifier_on);
                                humidifierCard.setStrokeColor(Color.parseColor("#2196F3"));
                            } else {
                                humidifierIcon.setImageResource(R.drawable.ic_humidifier_off);
                                humidifierCard.setStrokeColor(Color.parseColor("#CAC4D0"));
                            }
                        }
                    }
                    else {
                        humidifierSameCount = 0;
                    }
                    humidifierPreState = Boolean.parseBoolean(Humidifier_Ctrl_Str);

                }
            }
            //...................
            /* ***********************************1.2设置属性值 结束******************************************* */
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();                    // 初始化UI组件并设置事件
        addEvent();                     // 绑定事件
        registerBroadcastReceiver();    // 注册广播接收器
        startIotService();              // 启动后台服务
    }
    /**
     * 初始化UI组件
     */
    private void initViews() {
        humiTextView = findViewById(R.id.humi_tv);
        tempTextView = findViewById(R.id.temp_tv);
        tempTimeTextView = findViewById(R.id.temp_time);
        humiTimeTextView = findViewById(R.id.humi_time);
        // 初始化视图
        humiLimitDownSeekBar = findViewById(R.id.humiLimitDownSeekBar);
        humiLimitDown_tv = findViewById(R.id.humiLimitDownValue);
        humi_limit_down = humiLimitDownSeekBar.getProgress();
        // 设置滑动条监听器
        humiLimitDownSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 实时更新显示值
                humiLimitDown_tv.setText("湿度下限:" + progress + "°C");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 用户开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 用户停止滑动时调用
                humi_limit_down = seekBar.getProgress();
            }
        });
        // 初始化视图
        tempLimitUpSeekBar = findViewById(R.id.tempLimitUpSeekBar);
        tempLimitUp_tv = findViewById(R.id.tempLimitUpValue);
        temp_limit_up = tempLimitUpSeekBar.getProgress();
        // 设置滑动条监听器
        tempLimitUpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 实时更新显示值
                tempLimitUp_tv.setText("温度上限:" + progress + "°C");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 用户开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 用户停止滑动时调用
                temp_limit_up = seekBar.getProgress();

            }
        });
        Para_Save_Btn = findViewById(R.id.btn_save_set);
        Para_Save_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.example.myapplication.DATA_SEND");
                intent.setPackage(getPackageName());
                intent.putExtra("Temp_Limit_Up", String.valueOf(temp_limit_up));
                intent.putExtra("Humi_Limit_Down", String.valueOf(humi_limit_down));
                sendBroadcast(intent);
            }
        });
    }
    /**
     * 添加事件
     */
    private void addEvent(){
        // 历史数据按钮点击事件
        Button btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }
    /**
     * 注册广播接收器
     */
    private void registerBroadcastReceiver(){
        /* 注册广播接收器 */
        IntentFilter filter = new IntentFilter("com.example.myapplication.DATA_RECEIVED");      //指定你希望接收的广播 com.example.aliyun_mqtt.DATA_RECEIVED         // 查询当前设备的Android是否大于8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);                  // 注册广播接收器
        }
    }
    /**
     * 启动Iot后台服务
     */
    private void startIotService() {
        Intent serviceIntent = new Intent(this, IotService.class);
        startService(serviceIntent);
    }


    /**
     * 注销广播接收器
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataReceiver);
    }
}
