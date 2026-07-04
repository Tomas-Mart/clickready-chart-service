package com.clickready.chart.domain.model;

/**
 * Enum для типов данных графика.
 */
public enum ChartDataType {
    /**
     * Затраты
     */
    COST("cost", "Затраты"),
    /**
     * Стоимость привлечения
     */
    CPA("cpa", "CPA"),
    /**
     * Возврат инвестиций
     */
    ROI("roi", "ROI"),
    /**
     * Конверсии
     */
    CONVERSIONS("conversions", "Конверсии");

    private final String key;
    private final String displayName;

    ChartDataType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получает тип по ключу.
     *
     * @param key ключ
     * @return тип данных
     * @throws IllegalArgumentException если тип не найден
     */
    public static ChartDataType fromKey(String key) {
        for (var type : values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип данных: " + key);
    }
}