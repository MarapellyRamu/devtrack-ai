import { useEffect, useState } from 'react'
import {
  FolderKanban,
  Bug,
  CheckCircle2,
  Clock,
  TrendingUp,
  Plus,
  ArrowRight,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../layouts/AppLayout'
import { useAuth } from '../contexts/AuthContext'
import { getProjects, type Project } from '../services/projectService'
import { getProjectIssues, type Issue } from '../services/issueService'

type Stats = {
  totalProjects: number
  totalIssues: number
  openIssues: number
  resolvedIssues: number
}

export default function DashboardPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()
  const [projects, setProjects] = useState<Project[]>([])
  const [recentIssues, setRecentIssues] = useState<Issue[]>([])
  const [stats, setStats] = useState<Stats>({ totalProjects: 0, totalIssues: 0, openIssues: 0, resolvedIssues: 0 })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!token) return
    let cancelled = false

    async function load() {
      try {
        const proj = await getProjects(token!, 0, 100)
        if (cancelled) return
        const extraProjectPages = await Promise.all(
          Array.from({ length: Math.max(0, (proj.page?.totalPages ?? 1) - 1) }, (_, index) =>
            getProjects(token!, index + 1, 100),
          ),
        )
        const allProjects = [
          ...proj.content,
          ...extraProjectPages.flatMap((page) => page.content),
        ]
        setProjects(allProjects)

        const issueStats = await Promise.all(allProjects.map(async (project) => {
          const [all, open, resolved, closed, recent] = await Promise.all([
            getProjectIssues(token!, project.id, 0, 1),
            getProjectIssues(token!, project.id, 0, 1, 'OPEN'),
            getProjectIssues(token!, project.id, 0, 1, 'RESOLVED'),
            getProjectIssues(token!, project.id, 0, 1, 'CLOSED'),
            getProjectIssues(token!, project.id, 0, 8),
          ])
          return {
            total: all.page?.totalElements ?? all.content.length,
            open: open.page?.totalElements ?? open.content.length,
            resolved: (resolved.page?.totalElements ?? resolved.content.length) + (closed.page?.totalElements ?? closed.content.length),
            recent: recent.content,
          }
        }))
        if (cancelled) return

        const allRecentIssues: Issue[] = issueStats.flatMap((result) => result.recent)
        allRecentIssues.sort((a, b) => b.createdAt.localeCompare(a.createdAt))

        setRecentIssues(allRecentIssues.slice(0, 8))
        setStats({
          totalProjects: proj.page?.totalElements ?? proj.content.length,
          totalIssues: issueStats.reduce((sum, result) => sum + result.total, 0),
          openIssues: issueStats.reduce((sum, result) => sum + result.open, 0),
          resolvedIssues: issueStats.reduce((sum, result) => sum + result.resolved, 0),
        })
      } catch {
        // silently fail — dashboard is not critical
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => { cancelled = true }
  }, [token])

  const greet = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  }

  const statCards = [
    {
      label: 'Total Projects',
      value: stats.totalProjects,
      icon: <FolderKanban size={20} />,
      color: '#7c3aed',
      bg: 'rgba(124,58,237,0.15)',
    },
    {
      label: 'Total Issues',
      value: stats.totalIssues,
      icon: <Bug size={20} />,
      color: '#06b6d4',
      bg: 'rgba(6,182,212,0.15)',
    },
    {
      label: 'Open Issues',
      value: stats.openIssues,
      icon: <Clock size={20} />,
      color: '#f59e0b',
      bg: 'rgba(245,158,11,0.15)',
    },
    {
      label: 'Resolved',
      value: stats.resolvedIssues,
      icon: <CheckCircle2 size={20} />,
      color: '#10b981',
      bg: 'rgba(16,185,129,0.15)',
    },
  ]

  const priorityBadge = (priority: string) => {
    const map: Record<string, string> = {
      LOW: 'priority-low',
      MEDIUM: 'priority-medium',
      HIGH: 'priority-high',
      CRITICAL: 'priority-critical',
    }
    return map[priority] ?? 'priority-low'
  }

  return (
    <AppLayout
      title={`${greet()}, ${user?.name?.split(' ')[0] ?? 'User'} 👋`}
      subtitle="Here's what's happening across your projects."
    >
      {/* Stats */}
      <div className="stats-grid">
        {statCards.map((s) => (
          <div className="stat-card" key={s.label} style={{ '--stat-color': s.color } as React.CSSProperties}>
            <div className="stat-icon" style={{ background: s.bg, color: s.color }}>
              {s.icon}
            </div>
            <div className="stat-body">
              <div className="stat-label">{s.label}</div>
              <div className="stat-value">{loading ? '—' : s.value}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="dashboard-grid">
        {/* Recent Projects */}
        <div>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: 14,
            }}
          >
            <div className="section-title">
              <FolderKanban size={16} color="var(--brand-light)" />
              Recent Projects
            </div>
            <button
              className="btn btn-ghost btn-sm"
              onClick={() => navigate('/projects')}
              type="button"
            >
              View all <ArrowRight size={13} />
            </button>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : projects.length === 0 ? (
            <div className="empty-state" style={{ padding: '40px 20px' }}>
              <div className="empty-state-icon" style={{ width: 48, height: 48 }}>
                <FolderKanban size={22} />
              </div>
              <p style={{ fontSize: 13 }}>No projects yet. Create your first one!</p>
              <button className="btn btn-primary btn-sm" onClick={() => navigate('/projects')} type="button">
                <Plus size={14} /> New Project
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {projects.slice(0, 5).map((p) => (
                <div
                  key={p.id}
                  className="card"
                  style={{ padding: '14px 18px', cursor: 'pointer', borderRadius: 'var(--radius-md)' }}
                  onClick={() => navigate(`/projects/${p.id}/issues`)}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 14, color: 'var(--text-primary)', marginBottom: 4 }}>
                        {p.name}
                      </div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        {p.description || 'No description'}
                      </div>
                    </div>
                    <span className={`badge badge-${p.status.toLowerCase()}`}>{p.status}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Issues */}
        <div>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: 14,
            }}
          >
            <div className="section-title">
              <TrendingUp size={16} color="var(--accent)" />
              Recent Issues
            </div>
            <button className="btn btn-ghost btn-sm" onClick={() => navigate('/issues')} type="button">
              View all <ArrowRight size={13} />
            </button>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : recentIssues.length === 0 ? (
            <div className="empty-state" style={{ padding: '40px 20px' }}>
              <div className="empty-state-icon" style={{ width: 48, height: 48 }}>
                <Bug size={22} />
              </div>
              <p style={{ fontSize: 13 }}>No issues tracked yet.</p>
            </div>
          ) : (
            <div className="issue-list">
              {recentIssues.map((issue) => (
                <div key={issue.id} className="issue-card">
                  <div className={`issue-priority-dot dot-${issue.priority.toLowerCase()}`} />
                  <div className="issue-info">
                    <div className="issue-title">{issue.title}</div>
                    <div className="issue-meta">
                      <span className={`badge ${priorityBadge(issue.priority)}`}>{issue.priority}</span>
                    </div>
                  </div>
                  <span className={`badge badge-${issue.status.toLowerCase().replace('_', '-')}`}>
                    {issue.status.replace('_', ' ')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </AppLayout>
  )
}
