import { useAuth } from '../contexts/AuthContext'
import AppLayout from '../layouts/AppLayout'
import { User, Mail, CalendarDays, Shield } from 'lucide-react'

export default function SettingsPage() {
  const { user } = useAuth()

  return (
    <AppLayout title="Settings" subtitle="Manage your account and preferences.">
      <div style={{ maxWidth: 600 }}>
        <div className="card" style={{ marginBottom: 20 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 24 }}>
            <div style={{
              width: 60,
              height: 60,
              borderRadius: '50%',
              background: 'linear-gradient(135deg, var(--brand) 0%, var(--accent) 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 22,
              fontWeight: 700,
              color: 'white',
              boxShadow: '0 0 24px var(--brand-glow)',
            }}>
              {user?.name ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2) : 'U'}
            </div>
            <div>
              <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-primary)' }}>{user?.name}</div>
              <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>DevTrack AI member</div>
            </div>
          </div>

          <hr className="divider" />

          <div className="detail-field">
            <span className="detail-field-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <User size={12} /> Full Name
            </span>
            <div className="detail-field-value">{user?.name ?? '—'}</div>
          </div>

          <div className="detail-field">
            <span className="detail-field-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Mail size={12} /> Email Address
            </span>
            <div className="detail-field-value">{user?.email ?? '—'}</div>
          </div>

          <div className="detail-field">
            <span className="detail-field-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <CalendarDays size={12} /> Member Since
            </span>
            <div className="detail-field-value">
              {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : '—'}
            </div>
          </div>

          <div className="detail-field">
            <span className="detail-field-label" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <Shield size={12} /> Account ID
            </span>
            <div className="detail-field-value" style={{ fontFamily: 'var(--font-mono)', fontSize: 13 }}>
              #{user?.id ?? '—'}
            </div>
          </div>
        </div>

        <div className="card">
          <div style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: 12 }}>About DevTrack AI</div>
          <p style={{ fontSize: 13 }}>
            DevTrack AI is an intelligent project and issue management platform built with Spring Boot, React, and FastAPI.
            It provides a clean, fast interface for tracking bugs, tasks, and project progress.
          </p>
          <div style={{ marginTop: 14, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            {['v0.1.0', 'Spring Boot 3.5', 'React 19', 'PostgreSQL 16'].map((tag) => (
              <span key={tag} className="badge badge-open" style={{ fontSize: 11 }}>{tag}</span>
            ))}
          </div>
        </div>
      </div>
    </AppLayout>
  )
}
