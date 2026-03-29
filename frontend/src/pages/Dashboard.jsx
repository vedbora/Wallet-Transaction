import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, clearAuth, getStoredUser } from '../api';

const emptyForm = { amount: '', description: '', counterpartyEmail: '' };

function formatMoney(v) {
  if (v == null || v === '') return '—';
  const n = Number(v);
  if (Number.isNaN(n)) return String(v);
  return n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 4 });
}

export default function Dashboard() {
  const navigate = useNavigate();
  const user = getStoredUser();
  const [items, setItems] = useState([]);
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ type: '', from: '', to: '' });
  const [txMode, setTxMode] = useState('CREDIT');
  const [createForm, setCreateForm] = useState(emptyForm);
  const [editing, setEditing] = useState(null);

  const loadBalance = useCallback(async () => {
    try {
      const data = await api('/transactions/balance');
      setBalance(data.balance);
    } catch {
      setBalance(null);
    }
  }, []);

  const load = useCallback(async () => {
    setError('');
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (filters.type) params.set('type', filters.type);
      if (filters.from) params.set('from', new Date(filters.from).toISOString());
      if (filters.to) params.set('to', new Date(filters.to).toISOString());
      const q = params.toString();
      const data = await api(`/transactions${q ? `?${q}` : ''}`);
      setItems(data);
    } catch (e) {
      setError(e.message || 'Failed to load transactions');
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    loadBalance();
  }, [loadBalance]);

  function logout() {
    clearAuth();
    navigate('/login', { replace: true });
  }

  async function createTx(e) {
    e.preventDefault();
    setError('');
    const desc = createForm.description.trim();
    if (!desc) {
      setError('Description is required');
      return;
    }
    if (txMode === 'TRANSFER') {
      const em = createForm.counterpartyEmail.trim();
      if (!em) {
        setError('Recipient email is required for transfer');
        return;
      }
    }
    try {
      const payload = {
        amount: createForm.amount,
        type: txMode,
        description: desc,
      };
      if (txMode === 'TRANSFER') {
        payload.counterpartyEmail = createForm.counterpartyEmail.trim();
      }
      await api('/transactions', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setCreateForm(emptyForm);
      await load();
      await loadBalance();
    } catch (e) {
      setError(e.message || 'Create failed');
    }
  }

  async function updateTx(e) {
    e.preventDefault();
    if (!editing) return;
    setError('');
    try {
      await api(`/transactions/${editing.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          amount: editing.amount,
          type: editing.type,
          description: editing.description.trim(),
        }),
      });
      setEditing(null);
      await load();
      await loadBalance();
    } catch (e) {
      setError(e.message || 'Update failed');
    }
  }

  async function removeTx(id) {
    if (!window.confirm('Delete this transaction? (If it was a transfer, both sides are removed.)')) return;
    setError('');
    try {
      await api(`/transactions/${id}`, { method: 'DELETE' });
      await load();
      await loadBalance();
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
            <p className="eyebrow">Wallet</p>
            <h1>Transactions</h1>
            <p className="hero-sub">
              Signed in as <span className="accent">{user?.name}</span> · {user?.email} ·{' '}
              <span className="role-pill">{user?.role}</span>
            </p>
          </div>
          <div className="hero-actions">
            <div className="balance-card">
              <span className="balance-label">Available balance</span>
              <span className="balance-value">{balance != null ? formatMoney(balance) : '—'}</span>
            </div>
            {user?.role === 'ADMIN' && (
              <button type="button" className="btn ghost" onClick={() => navigate('/admin')}>
                Admin Portal
              </button>
            )}
            <button type="button" className="btn ghost" onClick={logout}>
              Log out
            </button>
          </div>
        </div>
      </header>

      <main className="main-content">
        {error && (
          <div className="error-banner" role="alert">
            {error}
          </div>
        )}

        <section className="panel">
          <h2 className="panel-title">New transaction</h2>
          <p className="panel-desc">Credit adds money, debit spends, transfer sends to another user’s wallet.</p>

          <div className="segmented" role="tablist">
            {['CREDIT', 'DEBIT', 'TRANSFER'].map((m) => (
              <button
                key={m}
                type="button"
                role="tab"
                aria-selected={txMode === m}
                className={txMode === m ? 'seg active' : 'seg'}
                onClick={() => setTxMode(m)}
              >
                {m === 'CREDIT' ? 'Credit' : m === 'DEBIT' ? 'Debit' : 'Transfer'}
              </button>
            ))}
          </div>

          <form onSubmit={createTx} className="create-form">
            <div className="field-grid">
              <label className="field">
                <span>Amount</span>
                <input
                  type="text"
                  inputMode="decimal"
                  placeholder="0.00"
                  required
                  value={createForm.amount}
                  onChange={(e) => setCreateForm({ ...createForm, amount: e.target.value })}
                />
              </label>
              {txMode === 'TRANSFER' && (
                <label className="field span-2">
                  <span>Recipient email</span>
                  <input
                    type="email"
                    autoComplete="email"
                    placeholder="friend@example.com"
                    required
                    value={createForm.counterpartyEmail}
                    onChange={(e) => setCreateForm({ ...createForm, counterpartyEmail: e.target.value })}
                  />
                </label>
              )}
              <label className="field span-2">
                <span>Description</span>
                <input
                  type="text"
                  required
                  maxLength={500}
                  placeholder="What is this for?"
                  value={createForm.description}
                  onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
                />
              </label>
            </div>
            <button type="submit" className="btn primary lg">
              {txMode === 'TRANSFER' ? 'Send transfer' : txMode === 'CREDIT' ? 'Add credit' : 'Record debit'}
            </button>
          </form>
        </section>

        <section className="panel">
          <h2 className="panel-title">Filters</h2>
          <div className="filters-row">
            <label className="field compact">
              <span>Type</span>
              <select
                value={filters.type}
                onChange={(e) => setFilters({ ...filters, type: e.target.value })}
              >
                <option value="">All</option>
                <option value="CREDIT">Credit</option>
                <option value="DEBIT">Debit</option>
                <option value="TRANSFER">Transfer</option>
              </select>
            </label>
            <label className="field compact">
              <span>From</span>
              <input
                type="datetime-local"
                value={filters.from}
                onChange={(e) => setFilters({ ...filters, from: e.target.value })}
              />
            </label>
            <label className="field compact">
              <span>To</span>
              <input
                type="datetime-local"
                value={filters.to}
                onChange={(e) => setFilters({ ...filters, to: e.target.value })}
              />
            </label>
            <button type="button" className="btn secondary" onClick={load}>
              Apply
            </button>
          </div>
        </section>

        <section className="panel">
          <h2 className="panel-title">Activity</h2>
          {loading ? (
            <p className="muted center-pad">Loading…</p>
          ) : items.length === 0 ? (
            <p className="muted center-pad">No transactions yet. Add a credit to get started.</p>
          ) : (
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>When</th>
                    <th>Type</th>
                    <th>Amount</th>
                    <th>Balance after</th>
                    <th>Counterparty</th>
                    <th>Description</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {items.map((tx) => (
                    <tr key={tx.id}>
                      <td className="nowrap">{tx.createdAt ? new Date(tx.createdAt).toLocaleString() : '—'}</td>
                      <td>
                        <span className={`type-badge type-${tx.type}`}>{tx.type}</span>
                      </td>
                      <td className={tx.type === 'CREDIT' ? 'amt-credit' : 'amt-debit'}>
                        {tx.type === 'CREDIT' ? '+' : '−'}
                        {formatMoney(tx.amount)}
                      </td>
                      <td className="mono">{formatMoney(tx.balanceAfter)}</td>
                      <td className="small">{tx.counterpartyEmail || '—'}</td>
                      <td className="desc-cell">{tx.description}</td>
                      <td className="actions">
                        {!tx.transferGroupId ? (
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() =>
                              setEditing({
                                id: tx.id,
                                amount: String(tx.amount),
                                type: tx.type,
                                description: tx.description,
                              })
                            }
                          >
                            Edit
                          </button>
                        ) : (
                          <span className="muted tiny" title="Transfers cannot be edited">
                            —
                          </span>
                        )}
                        <button type="button" className="btn-link danger" onClick={() => removeTx(tx.id)}>
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>

      {editing && (
        <div className="modal-backdrop" role="presentation" onClick={() => setEditing(null)}>
          <div className="panel modal" onClick={(e) => e.stopPropagation()}>
            <h2 className="panel-title">Edit #{editing.id}</h2>
            <form onSubmit={updateTx} className="stack">
              <label className="field">
                <span>Amount</span>
                <input
                  type="text"
                  inputMode="decimal"
                  required
                  value={editing.amount}
                  onChange={(e) => setEditing({ ...editing, amount: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Type</span>
                <select
                  value={editing.type}
                  onChange={(e) => setEditing({ ...editing, type: e.target.value })}
                >
                  <option value="CREDIT">CREDIT</option>
                  <option value="DEBIT">DEBIT</option>
                </select>
              </label>
              <label className="field">
                <span>Description</span>
                <input
                  required
                  maxLength={500}
                  value={editing.description}
                  onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                />
              </label>
              <div className="btn-row">
                <button type="submit" className="btn primary">
                  Save
                </button>
                <button type="button" className="btn ghost" onClick={() => setEditing(null)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
