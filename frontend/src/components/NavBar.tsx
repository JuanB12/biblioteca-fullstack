import { NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function NavBar() {
  const { name, role, logout } = useAuthStore((s) => ({ name: s.name, role: s.role, logout: s.logout }));
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  return (
    <header className="navbar">
      <div className="navbar-brand">📚 Biblioteca</div>
      <nav className="navbar-links">
        <NavLink to="/" end>
          Catálogo
        </NavLink>
        <NavLink to="/mis-prestamos">Mis préstamos</NavLink>
        {role === 'ADMIN' && <NavLink to="/admin">Admin</NavLink>}
      </nav>
      <div className="navbar-user">
        <span>{name}</span>
        <button type="button" onClick={handleLogout} className="btn btn-ghost">
          Salir
        </button>
      </div>
    </header>
  );
}
