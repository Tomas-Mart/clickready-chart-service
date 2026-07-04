-- V1__create_chart_data_table.sql
-- Создание таблицы для хранения данных графика

CREATE TABLE IF NOT EXISTS chart_data (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    cpa DECIMAL(10, 2) NOT NULL,
    roi DECIMAL(10, 2) NOT NULL,
    conversions INTEGER NOT NULL,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_chart_data_date UNIQUE (date)
);

-- Комментарии к таблице и колонкам
COMMENT ON TABLE chart_data IS 'Данные для графика ClickReady';
COMMENT ON COLUMN chart_data.date IS 'Дата в формате YYYY-MM-DD';
COMMENT ON COLUMN chart_data.cost IS 'Затраты в USD';
COMMENT ON COLUMN chart_data.cpa IS 'Cost Per Acquisition - стоимость привлечения клиента';
COMMENT ON COLUMN chart_data.roi IS 'Return on Investment - возврат инвестиций в процентах';
COMMENT ON COLUMN chart_data.conversions IS 'Количество конверсий';
COMMENT ON COLUMN chart_data.version IS 'Версия для оптимистичной блокировки';