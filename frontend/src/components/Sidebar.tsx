import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard,
  FolderKanban,
  Bug,
  Settings,
  Zap,
  LogOut,
  Bell,
} from 'lucide-react'
import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getUnreadNotificationCount } from '../services/notificationService'

export default function Sidebar() {
  const { user, token, signOut } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    if (!token) return
    let active = true
    const refreshCount = () => {
      getUnreadNotificationCount(token).then((result) => { if (active) setUnreadCount(result.count) }).catch(() => { if (active) setUnreadCount(0) })
    }
    refreshCount()
    window.addEventListener('devtrack:notifications-changed', refreshCount)
    return () => {
      active = false
      window.removeEventListener('devtrack:notifications-changed', refreshCount)
    }
  }, [token, location.pathname])

  function handleSignOut() {
    signOut()
    navigate('/login')
  }

  const initials = user?.name
    ? user.name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
    : 'U'

  return (
    <aside className="sidebar">
      {/* Logo */}
      <div className="sidebar-logo">
        <div className="logo-mark">
          <div className="logo-icon">
            <Zap size={18} color="white" strokeWidth={2.5} />
          </div>
          <div className="logo-text">
            <span className="logo-name">DevTrack AI</span>
            <span className="logo-tagline">Project Management</span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        <div className="nav-section">
          <span className="nav-section-label">Overview</span>
          <NavLink
            to="/dashboard"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <LayoutDashboard size={16} />
            Dashboard
          </NavLink>
        </div>

        <div className="nav-section">
          <span className="nav-section-label">Work</span>
          <NavLink
            to="/projects"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <FolderKanban size={16} />
            Projects
          </NavLink>
          <NavLink
            to="/issues"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <Bug size={16} />
            All Issues
          </NavLink>
          <NavLink to="/notifications" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
            <Bell size={16} />
            Notifications
            {unreadCount > 0 && <span className="nav-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>}
          </NavLink>
        </div>

        <div className="nav-section">
          <span className="nav-section-label">Account</span>
          <NavLink
            to="/settings"
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <Settings size={16} />
            Settings
          </NavLink>
        </div>
      </nav>

      {/* User menu */}
      <div className="sidebar-footer">
        <button className="user-menu" onClick={handleSignOut} type="button" title="Sign out">
          <div className="avatar">{initials}</div>
          <div className="user-info">
            <div className="user-name">{user?.name ?? 'User'}</div>
            <div className="user-email">{user?.email ?? ''}</div>
          </div>
          <LogOut size={14} style={{ color: 'var(--text-muted)', flexShrink: 0 }} />
        </button>
      </div>
    </aside>
  )
}
