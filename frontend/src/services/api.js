import axios from 'axios';

const api = axios.create({
  baseURL: (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_BASE_URL) || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Request interceptor to attach bearer token
api.interceptors.request.use(
  (config) => {
    try {
      const token = localStorage.getItem('finova_auth_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch {
      // Ignore storage access errors
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      try {
        localStorage.removeItem('finova_auth_token');
        localStorage.removeItem('finova_auth_user');
      } catch {
        // Ignore
      }

      // Avoid infinite redirect loops if already on login page or if error occurred during login attempt
      const isLoginRequest = error.config?.url?.includes('/auth/login');
      if (
        typeof window !== 'undefined' &&
        window.location.pathname !== '/login' &&
        !isLoginRequest
      ) {
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

export default api;
