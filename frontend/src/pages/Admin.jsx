import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, getStoredUser } from '../api';

export default function Admin() {
  const navigate = useNavigate();
  const user = getStoredUser();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      navigate('/', { replace: true });
    }
  }, [user, navigate]);

  const loadUsers = async () => {
    try {
      const data = await api('/users');
      setUsers(data);
    } catch (e) {
      setError(e.message || 'Failed to load users');
    }
  };

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      loadUsers();
    }
  }, [user]);

  async function removeUser(id) {
    if (!window.confirm('Delete this user and ALL their transactions? This cannot be undone.')) return;
    try {
      await api(`/users/${id}`, { method: 'DELETE' });
      await loadUsers();
    } catch (e) {
      setError(e.message || 'Delete failed');
    }
  }

  return (
    <div className="app-shell">
      <div className="bg-aurora" aria-hidden />
      <header className="hero">
        <div className="hero-inner">
          <div>
            <p className="eyebrow">Admin Portal</p>
            <h1>User Management</h1>
            <p className="hero-sub">Manage all registered users and easily delete their associated data.</p>
          </div>
          <div className="hero-actions">
            <button type="button" className="btn ghost" onClick={() => navigate('/')}>
              Back to Dashboard
            </button>
          </div>
        </div>
      </header>

      <main className="main-content">
        {error && <div className="error-banner" role="alert">{error}</div>}
        
        <section className="panel">
          <h2 className="panel-title">Users</h2>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.id}</td>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td><span className="role-pill">{u.role}</span></td>
                    <td className="actions">
                      {u.id !== user?.userId && (
                        <button type="button" className="btn-link danger" onClick={() => removeUser(u.id)}>
                          Delete User
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  );
}
