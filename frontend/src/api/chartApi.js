import apiClient from './client';

const CHART_API = '/v1/chart';

export const chartApi = {
    /**
     * Получить все данные графика
     * GET /api/v1/chart/data
     */
    getAll: () => apiClient.get(`${CHART_API}/data`),

    /**
     * Получить данные за период
     * GET /api/v1/chart/data/range?startDate=...&endDate=...
     */
    getRange: (startDate, endDate) =>
        apiClient.get(`${CHART_API}/data/range`, {
            params: {startDate, endDate}
        }),

    /**
     * Создать запись
     * POST /api/v1/chart/data
     */
    create: (data) => apiClient.post(`${CHART_API}/data`, data),

    /**
     * Пакетное создание записей
     * POST /api/v1/chart/data/batch
     */
    createBatch: (data) => apiClient.post(`${CHART_API}/data/batch`, data),

    /**
     * Удалить запись по дате
     * DELETE /api/v1/chart/data/{date}
     */
    delete: (date) => apiClient.delete(`${CHART_API}/data/${date}`),
};

export default chartApi;