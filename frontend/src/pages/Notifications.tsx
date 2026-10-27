import { useCallback, useEffect, useState } from 'react'
import { Bell, Check } from 'lucide-react'
import AppLayout from '../layouts/AppLayout'
import { useAuth } from '../contexts/AuthContext'
import { getNotifications, markNotificationRead, type NotificationItem } from '../services/notificationService'
import { useNavigate } from 'react-router-dom'

export default function NotificationsPage() {
  const { token } = useAuth()
  const navigate = useNavigate()
  const [items, setItems] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError('')
    try { setItems((await getNotifications(token)).content) }
    catch (err) { setError(err instanceof Error ? err.message : 'Could not load notifications.') }
    finally { setLoading(false) }
  }, [token])
  useEffect(() => {
    let cancelled = false
    queueMicrotask(() => { if (!cancelled) void load() })
    return () => { cancelled = true }
  }, [load])

  async function markRead(item: NotificationItem) {
    if (!token || item.readAt) return
    try {
      await markNotificationRead(token, item.id)
      setItems((current) => current.map((entry) => entry.id === item.id ? { ...entry, readAt: new Date().toISOString() } : entry))
      window.dispatchEvent(new Event('devtrack:notifications-changed'))
    } catch (err) { setError(err instanceof Error ? err.message : 'Could not mark notification as read.') }
  }

  return <AppLayout title="Notifications" subtitle="Updates from your shared projects.">
    {loading && <div className="loading-center"><div className="spinner" /><span>Loading notifications...</span></div>}
    {error && <div className="alert alert-error" role="alert">{error}<button className="btn btn-ghost btn-sm" type="button" onClick={load}>Retry</button></div>}
    {!loading && !error && items.length === 0 && <div className="empty-state"><div className="empty-state-icon"><Bell size={28} /></div><h3>You're all caught up</h3><p>Project changes from your teammates will appear here.</p></div>}
    {!loading && items.length > 0 && <div className="issue-list">
      {items.map((item) => <article className="issue-card" key={item.id} style={{ cursor: 'default', opacity: item.readAt ? 0.72 : 1 }}>
        <div className={`issue-priority-dot ${item.readAt ? 'dot-low' : 'dot-high'}`} />
        <div className="issue-info"><div className="issue-title">{item.summary}</div><div className="issue-meta">{item.actorEmail} · {new Date(item.createdAt).toLocaleString()}</div></div>
        <button className="btn btn-secondary btn-sm" type="button" onClick={() => item.readAt ? navigate(`/projects/${item.projectId}/issues`) : void markRead(item)}>
          {item.readAt ? 'Open project' : <><Check size={14} /> Mark read</>}
        </button>
      </article>)}
    </div>}
  </AppLayout>
}
