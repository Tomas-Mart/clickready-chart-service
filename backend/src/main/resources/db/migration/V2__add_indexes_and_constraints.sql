-- V2__add_indexes_and_constraints.sql
-- Добавление индексов для оптимизации запросов

-- Индекс для быстрого поиска по дате
CREATE INDEX IF NOT EXISTS idx_chart_data_date ON chart_data(date);

-- Составной индекс для запросов по дате и затратам
CREATE INDEX IF NOT EXISTS idx_chart_data_date_cost ON chart_data(date, cost);

-- Индекс для поиска по ROI (для аналитики)
CREATE INDEX IF NOT EXISTS idx_chart_data_roi ON chart_data(roi);

-- Проверка, что conversions не отрицательное
ALTER TABLE chart_data
ADD CONSTRAINT chk_chart_data_conversions_positive
CHECK (conversions >= 0);

-- Проверка, что CPA не отрицательный
ALTER TABLE chart_data
ADD CONSTRAINT chk_chart_data_cpa_positive
CHECK (cpa >= 0);