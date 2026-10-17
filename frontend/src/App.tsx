import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { NavBar } from './components/NavBar';
import { AdminRoute, ProtectedRoute } from './components/ProtectedRoute';
import { AdminPage } from './pages/AdminPage';
import { CatalogPage } from './pages/CatalogPage';
import { LoginPage } from './pages/LoginPage';
import { MyLoansPage } from './pages/MyLoansPage';

function AppLayout() {
  return (
    <div className="app-shell">
      <NavBar />
      <main className="app-content">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/" element={<CatalogPage />} />
            <Route path="/mis-prestamos" element={<MyLoansPage />} />
            <Route element={<AdminRoute />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
