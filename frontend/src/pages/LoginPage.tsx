import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../api/authApi';
import { extractErrorMessage } from '../api/httpClient';
import { useAuthStore } from '../store/authStore';

export function LoginPage() {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = useAuthStore((s) => s.login);
  const navigate = useNavigate();

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const response =
        mode === 'login'
          ? await authApi.login({ email, password })
          : await authApi.register({ name, email, password });
      login(response);
      navigate('/', { replace: true });
    } catch (err) {
      setError(extractErrorMessage(err, 'No se pudo iniciar sesion'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>📚 Biblioteca</h1>
        <p className="auth-subtitle">
          {mode === 'login' ? 'Inicia sesion para continuar' : 'Crea una cuenta de bibliotecario'}
        </p>

        {mode === 'register' && (
          <label className="field">
            Nombre
            <input value={name} onChange={(e) => setName(e.target.value)} required minLength={1} />
          </label>
        )}

        <label className="field">
          Correo
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>

        <label className="field">
          Contrasena
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
          />
        </label>

        {error && <div className="state state-error">⚠ {error}</div>}

        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Un momento...' : mode === 'login' ? 'Ingresar' : 'Registrarme'}
        </button>

        <button
          type="button"
          className="btn btn-link"
          onClick={() => {
            setMode(mode === 'login' ? 'register' : 'login');
            setError(null);
          }}
        >
          {mode === 'login' ? '¿No tienes cuenta? Registrate' : 'Ya tengo cuenta, iniciar sesion'}
        </button>
      </form>
    </div>
  );
}
