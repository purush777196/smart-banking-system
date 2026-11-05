/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { DEMO_USERS, getPersonaByEmail, getPersonaByRole } from '../data/mockAuth';
import { getRouteForRole } from '../routes/roleMapping';
import api from '../services/api';

export const AuthContext = createContext();

const enrichUserData = (backendUser) => {
  if (!backendUser) return null;
  const persona =
    getPersonaByEmail(backendUser.email) ||
    getPersonaByRole(backendUser.role);

  return {
    ...persona,
    id: backendUser.id,
    name: backendUser.fullName || persona?.name || 'Finova User',
    email: backendUser.email,
    role: backendUser.role,
    enabled: backendUser.enabled ?? true,
    createdAt: backendUser.createdAt,
    roleTitle: persona?.roleTitle || backendUser.role,
    badge: persona?.badge || 'Authorized',
    defaultRoute: getRouteForRole(backendUser.role),
    avatar:
      persona?.avatar ||
      'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80',
  };
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem('finova_auth_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => {
    try {
      return localStorage.getItem('finova_auth_token') || null;
    } catch {
      return null;
    }
  });

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Validate session on mount with /api/auth/me
  useEffect(() => {
    let isMounted = true;

    const initAuth = async () => {
      const storedToken = localStorage.getItem('finova_auth_token');
      const storedUser = localStorage.getItem('finova_auth_user');

      if (!storedToken) {
        if (isMounted) {
          setUser(null);
          setToken(null);
          setIsLoading(false);
        }
        return;
      }

      // Pre-seed state from storage so UI doesn't flicker
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
          setToken(storedToken);
        } catch {
          // ignore corrupted JSON
        }
      }

      try {
        const response = await api.get('/auth/me');
        if (isMounted) {
          const enriched = enrichUserData(response.data);
          setUser(enriched);
          setToken(storedToken);
          localStorage.setItem('finova_auth_user', JSON.stringify(enriched));
        }
      } catch (err) {
        console.warn('Backend session validation:', err?.response?.data || err.message);
        if (err?.response?.status === 401) {
          localStorage.removeItem('finova_auth_token');
          localStorage.removeItem('finova_auth_user');
          if (isMounted) {
            setUser(null);
            setToken(null);
          }
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    initAuth();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = useCallback(async (email, password) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.post('/auth/login', {
        email: email.trim(),
        password,
      });

      const { token: receivedToken, user: backendUser } = response.data;
      const enriched = enrichUserData(backendUser);

      localStorage.setItem('finova_auth_token', receivedToken);
      localStorage.setItem('finova_auth_user', JSON.stringify(enriched));

      setToken(receivedToken);
      setUser(enriched);
      setIsLoading(false);
      return enriched;
    } catch (err) {
      setIsLoading(false);
      const message =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        (err?.response?.status === 401
          ? 'Invalid email or password'
          : 'Authentication failed. Please verify the backend server is running on port 8080.');
      setError(message);
      throw new Error(message, { cause: err });
    }
  }, []);

  const register = useCallback(async (fullName, email, password) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await api.post('/auth/register', {
        fullName: fullName.trim(),
        email: email.trim(),
        password,
      });

      const { token: receivedToken, user: backendUser } = response.data;
      const enriched = enrichUserData(backendUser);

      localStorage.setItem('finova_auth_token', receivedToken);
      localStorage.setItem('finova_auth_user', JSON.stringify(enriched));

      setToken(receivedToken);
      setUser(enriched);
      setIsLoading(false);
      return enriched;
    } catch (err) {
      setIsLoading(false);
      const message =
        err?.response?.data?.message ||
        err?.response?.data?.error ||
        'Registration failed. Please check your credentials.';
      setError(message);
      throw new Error(message, { cause: err });
    }
  }, []);

  const switchRole = useCallback(async (roleKeyOrId) => {
    const target =
      DEMO_USERS.find(
        (u) =>
          u.id === roleKeyOrId ||
          u.role === String(roleKeyOrId).toUpperCase() ||
          u.email.toLowerCase() === String(roleKeyOrId).toLowerCase()
      ) || DEMO_USERS[0];

    return await login(target.email, 'demo1234');
  }, [login]);

  const loginAsRole = switchRole;

  const logout = useCallback(() => {
    try {
      localStorage.removeItem('finova_auth_token');
      localStorage.removeItem('finova_auth_user');
    } catch {
      // Ignore
    }
    setToken(null);
    setUser(null);
    setError(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        isLoading,
        error,
        login,
        register,
        loginAsRole,
        switchRole,
        logout,
        demoUsers: DEMO_USERS,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
