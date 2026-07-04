import axios from 'axios';

// ✅ Базовый URL для API
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Создаем экземпляр axios с настройками
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    timeout: 30000, // 30 секунд
});

// ✅ Interceptor для добавления JWT токена
apiClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// ✅ Interceptor для обработки ошибок
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        // Если 401 Unauthorized - перенаправляем на логин
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default apiClient;