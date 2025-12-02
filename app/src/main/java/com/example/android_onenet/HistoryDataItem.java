package com.example.android_onenet;

/**
 * 历史数据项实体类
 */
public class HistoryDataItem {
    private String identifier;  // 数据标识符 (如 Temp, Humi)
    private String value;       // 数据值
    private long timestamp;     // 时间戳

    public HistoryDataItem(String identifier, String value, long timestamp) {
        this.identifier = identifier;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        switch (identifier) {
            case "Temp":
                return "温度";
            case "Humi":
                return "湿度";
            case "Led_Ctrl":
                return "灯光";
            case "Fan_Ctrl":
                return "风扇";
            case "Humidifier_Ctrl":
                return "加湿器";
            default:
                return identifier;
        }
    }

    /**
     * 获取带单位的显示值
     */
    public String getDisplayValue() {
        switch (identifier) {
            case "Temp":
                return value + "°C";
            case "Humi":
                return value + "%";
            case "Led_Ctrl":
            case "Fan_Ctrl":
            case "Humidifier_Ctrl":
                return "true".equals(value) ? "开启" : "关闭";
            default:
                return value;
        }
    }
}
