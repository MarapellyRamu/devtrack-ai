import { useEffect, useState, useCallback, useMemo } from 'react'
import { Bug, Search, SlidersHorizontal, CalendarDays } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import AppLayout from '../layouts/AppLayout'
import { useAuth } from '../contexts/AuthContext'
import { getProjects } from '../services/projectService'
import { getProjectIssues, type Issue, type IssueStatus, type IssuePriority } from '../services/issueService'

const STATUSES: IssueStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']
const PRIORITIES: IssuePriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

function priorityClass(p: IssuePriority) {
  return { LOW: 'priority-low', MEDIUM: 'priority-medium', HIGH: 'priority-high', CRITICAL: 'priority-critical' }[p]
}

function statusClass(s: IssueStatus) {
  return { OPEN: 'badge-open', IN_PROGRESS: 'badge-in-progress', RESOLVED: 'badge-resolved', CLOSED: 'badge-closed' }[s]
}

export default function AllIssuesPage() {
  const { token } = useAuth()
  const navigate = useNavigate()

  const [issues, setIssues] = useState<(Issue & { projectName: string })[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState<IssueStatus | ''>('')
  const [filterPriority, setFilterPriority] = useState<IssuePriority | ''>('')

  const load = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError('')
    try {
      const projPage = await getProjects(token, 0, 100)
      const results = await Promise.all(
        projPage.content.map(async (p) => {
          const page = await getProjectIssues(token, p.id, 0, 200).catch(() => null)
          return (page?.content ?? []).map((i) => ({ ...i, projectName: p.name }))
        })
      )
      const all = results.flat().sort((a, b) => b.createdAt.localeCompare(a.createdAt))
      setIssues(all)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load issues.')
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    let cancelled = false
    queueMicrotask(() => { if (!cancelled) void load() })
    return () => { cancelled = true }
  }, [load])

  const filtered = useMemo(() => {
    let result = [...issues]
    if (search) {
      const q = search.toLowerCase()
      result = result.filter((i) => i.title.toLowerCase().includes(q) || i.projectName.toLowerCase().includes(q))
    }
    if (filterStatus) result = result.filter((i) => i.status === filterStatus)
    if (filterPriority) result = result.filter((i) => i.priority === filterPriority)
    return result
  }, [issues, search, filterStatus, filterPriority])

  return (
    <AppLayout title="All Issues" subtitle={`${filtered.length} issue${filtered.length !== 1 ? 's' : ''} across all projects`}>
      <div className="toolbar">
        <div className="search-input-wrap">
          <Search size={15} />
          <input
            type="search"
            placeholder="Search issues or projects..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            id="all-issues-search"
          />
        </div>
        <SlidersHorizontal size={15} style={{ color: 'var(--text-muted)', flexShrink: 0 }} />
        <select className="filter-select" value={filterStatus} onChange={(e) => setFilterStatus(e.target.value as IssueStatus | '')} id="all-filter-status">
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
        </select>
        <select className="filter-select" value={filterPriority} onChange={(e) => setFilterPriority(e.target.value as IssuePriority | '')} id="all-filter-priority">
          <option value="">All priorities</option>
          {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>

      {loading && <div className="loading-center"><div className="spinner" /><span>Loading all issues...</span></div>}
      {error && !loading && <div className="alert alert-error">{error}</div>}

      {!loading && !error && filtered.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon"><Bug size={32} /></div>
          <h3>No issues found</h3>
          <p>Create issues in a project to see them here.</p>
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div className="issue-list">
          {filtered.map((issue) => (
            <div
              key={`${issue.projectId}-${issue.id}`}
              className="issue-card"
              onClick={() => navigate(`/projects/${issue.projectId}/issues`)}
              role="button"
              tabIndex={0}
            >
              <div className={`issue-priority-dot dot-${issue.priority.toLowerCase()}`} />
              <div className="issue-info">
                <div className="issue-title">{issue.title}</div>
                <div className="issue-meta">
                  <span style={{ color: 'var(--brand-light)', fontSize: 12 }}>{issue.projectName}</span>
                  <span className={`badge ${priorityClass(issue.priority)}`}>{issue.priority}</span>
                  {issue.dueDate && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <CalendarDays size={11} /> {issue.dueDate}
                    </span>
                  )}
                </div>
              </div>
              <span className={`badge ${statusClass(issue.status)}`}>{issue.status.replace('_', ' ')}</span>
            </div>
          ))}
        </div>
      )}
    </AppLayout>
  )
}
