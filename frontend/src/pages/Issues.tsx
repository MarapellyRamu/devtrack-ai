import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Plus,
  Bug,
  Search,
  Pencil,
  Trash2,
  CalendarDays,
  ArrowLeft,
  SlidersHorizontal,
  Sparkles,
  History,
  Users,
} from 'lucide-react'
import AppLayout from '../layouts/AppLayout'
import Modal from '../components/Modal'
import { useAuth } from '../contexts/AuthContext'
import { getProject, type Project } from '../services/projectService'
import {
  getProjectIssues,
  createIssue,
  updateIssue,
  deleteIssue,
  type Issue,
  type IssueRequest,
  type IssuePriority,
  type IssueStatus,
} from '../services/issueService'
import { suggestIssues, type IssueSuggestion } from '../services/aiService'
import { getProjectActivity, type ActivityEvent } from '../services/activityService'
import { addMember, getMembers, removeMember, updateMemberRole, type ProjectMember, type ProjectRole } from '../services/memberService'

const PRIORITIES: IssuePriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const STATUSES: IssueStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED']

function priorityClass(p: IssuePriority) {
  return { LOW: 'priority-low', MEDIUM: 'priority-medium', HIGH: 'priority-high', CRITICAL: 'priority-critical' }[p]
}

function statusClass(s: IssueStatus) {
  return { OPEN: 'badge-open', IN_PROGRESS: 'badge-in-progress', RESOLVED: 'badge-resolved', CLOSED: 'badge-closed' }[s]
}

export default function IssuesPage() {
  const { projectId } = useParams<{ projectId: string }>()
  const { token, user } = useAuth()
  const navigate = useNavigate()

  const pid = Number(projectId)

  const [project, setProject] = useState<Project | null>(null)
  const [issues, setIssues] = useState<Issue[]>([])
  const [issueTotal, setIssueTotal] = useState(0)
  const [issueTotalPages, setIssueTotalPages] = useState(0)
  const [pageIndex, setPageIndex] = useState(0)
  const [activity, setActivity] = useState<ActivityEvent[]>([])
  const [showActivity, setShowActivity] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [search, setSearch] = useState('')
  const [filterStatus, setFilterStatus] = useState<IssueStatus | ''>('')
  const [filterPriority, setFilterPriority] = useState<IssuePriority | ''>('')

  const [modal, setModal] = useState<'create' | 'edit' | 'delete' | 'view' | null>(null)
  const [selected, setSelected] = useState<Issue | null>(null)
  const [form, setForm] = useState<IssueRequest>({ title: '', description: '', priority: 'MEDIUM', status: 'OPEN' })
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [aiOpen, setAiOpen] = useState(false)
  const [aiContext, setAiContext] = useState('')
  const [aiSuggestions, setAiSuggestions] = useState<IssueSuggestion[]>([])
  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState('')
  const [aiSaving, setAiSaving] = useState<number | null>(null)
  const [teamOpen, setTeamOpen] = useState(false)
  const [team, setTeam] = useState<ProjectMember[]>([])
  const [memberEmail, setMemberEmail] = useState('')
  const [memberRole, setMemberRole] = useState<ProjectRole>('MEMBER')
  const [teamError, setTeamError] = useState('')

  async function openTeam() {
    if (!token) return
    setTeamError('')
    setTeamOpen(true)
    try { setTeam(await getMembers(token, pid)) }
    catch (err) { setTeamError(err instanceof Error ? err.message : 'Could not load project members.') }
  }

  async function inviteMember(e: React.FormEvent) {
    e.preventDefault()
    if (!token) return
    setTeamError('')
    try {
      const member = await addMember(token, pid, memberEmail, memberRole)
      setTeam((members) => [...members, member])
      setMemberEmail('')
    } catch (err) { setTeamError(err instanceof Error ? err.message : 'Could not add member.') }
  }

  async function changeMemberRole(member: ProjectMember, role: ProjectRole) {
    if (!token) return
    try {
      const updated = await updateMemberRole(token, pid, member.userId, role)
      setTeam((members) => members.map((item) => item.userId === updated.userId ? updated : item))
    } catch (err) { setTeamError(err instanceof Error ? err.message : 'Could not update role.') }
  }

  async function deleteMember(member: ProjectMember) {
    if (!token) return
    try {
      await removeMember(token, pid, member.userId)
      setTeam((members) => members.filter((item) => item.userId !== member.userId))
    } catch (err) { setTeamError(err instanceof Error ? err.message : 'Could not remove member.') }
  }

  async function generateSuggestions(e: React.FormEvent) {
    e.preventDefault()
    if (!token || !project) return
    setAiLoading(true)
    setAiError('')
    try {
      const result = await suggestIssues(token, pid, aiContext)
      setAiSuggestions(result.issues)
    } catch (err) {
      setAiError(err instanceof Error ? err.message : 'Could not generate suggestions.')
    } finally { setAiLoading(false) }
  }

  async function addSuggestion(suggestion: IssueSuggestion, index: number) {
    if (!token) return
    setAiSaving(index)
    setAiError('')
    try {
      await createIssue(token, pid, suggestion)
      setAiSuggestions((items) => items.filter((_, itemIndex) => itemIndex !== index))
      await load()
    } catch (err) {
      setAiError(err instanceof Error ? err.message : 'Could not add this issue.')
    } finally { setAiSaving(null) }
  }

  const load = useCallback(async () => {
    if (!token || !pid) return
    setLoading(true)
    setError('')
    try {
      const [proj, page, activityPage] = await Promise.all([
        getProject(token, pid),
        getProjectIssues(token, pid, pageIndex, 20, filterStatus || undefined, filterPriority || undefined, search),
        getProjectActivity(token, pid),
      ])
      setProject(proj)
      setIssues(page.content)
      setIssueTotal(page.page?.totalElements ?? page.content.length)
      setIssueTotalPages(page.page?.totalPages ?? 1)
      setActivity(activityPage.content)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load data.')
    } finally {
      setLoading(false)
    }
  }, [token, pid, pageIndex, filterStatus, filterPriority, search])

  useEffect(() => {
    let cancelled = false
    queueMicrotask(() => { if (!cancelled) void load() })
    return () => { cancelled = true }
  }, [load])

  function openCreate() {
    setForm({ title: '', description: '', priority: 'MEDIUM', status: 'OPEN' })
    setFormError('')
    setModal('create')
  }

  function openEdit(issue: Issue, e: React.MouseEvent) {
    e.stopPropagation()
    setSelected(issue)
    setForm({ title: issue.title, description: issue.description ?? '', priority: issue.priority, status: issue.status, dueDate: issue.dueDate })
    setFormError('')
    setModal('edit')
  }

  function openView(issue: Issue) {
    setSelected(issue)
    setModal('view')
  }

  function openDelete(issue: Issue, e: React.MouseEvent) {
    e.stopPropagation()
    setSelected(issue)
    setModal('delete')
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    if (!token) return
    setSaving(true)
    setFormError('')
    try {
      await createIssue(token, pid, form)
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to create issue.')
    } finally { setSaving(false) }
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault()
    if (!token || !selected) return
    setSaving(true)
    setFormError('')
    try {
      await updateIssue(token, pid, selected.id, form)
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to update issue.')
    } finally { setSaving(false) }
  }

  async function handleDelete() {
    if (!token || !selected) return
    setSaving(true)
    try {
      await deleteIssue(token, pid, selected.id)
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to delete issue.')
    } finally { setSaving(false) }
  }

  const renderIssueForm = (onSubmit: (e: React.FormEvent) => void) => (
    <form onSubmit={onSubmit}>
      {formError && <div className="alert alert-error" style={{ marginBottom: 18 }}>{formError}</div>}
      <div className="form-group">
        <label htmlFor="issue-title">Title <span style={{ color: 'var(--danger)' }}>*</span></label>
        <input
          id="issue-title"
          type="text"
          value={form.title}
          onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
          placeholder="Brief summary of the issue"
          required
          maxLength={200}
          autoFocus
        />
      </div>
      <div className="form-group">
        <label htmlFor="issue-desc">Description</label>
        <textarea
          id="issue-desc"
          value={form.description}
          onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
          placeholder="Detailed description, steps to reproduce, etc."
          rows={4}
          style={{ resize: 'vertical' }}
        />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="issue-priority">Priority</label>
          <select
            id="issue-priority"
            className="filter-select"
            style={{ width: '100%' }}
            value={form.priority}
            onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value as IssuePriority }))}
          >
            {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label htmlFor="issue-status">Status</label>
          <select
            id="issue-status"
            className="filter-select"
            style={{ width: '100%' }}
            value={form.status}
            onChange={(e) => setForm((f) => ({ ...f, status: e.target.value as IssueStatus }))}
          >
            {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
          </select>
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="issue-due">Due date</label>
        <input
          id="issue-due"
          type="date"
          value={form.dueDate ?? ''}
          onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value || undefined }))}
        />
      </div>
      <div className="modal-actions">
        <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
        <button type="submit" className="btn btn-primary" disabled={saving || !form.title.trim()}>
          {saving ? <><div className="spinner" /> Saving...</> : modal === 'create' ? 'Create issue' : 'Save changes'}
        </button>
      </div>
    </form>
  )

  return (
    <AppLayout
      title={project?.name ?? 'Issues'}
      subtitle={project ? `${issueTotal} issue${issueTotal !== 1 ? 's' : ''}` : 'Loading...'}
      actions={
        <>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/projects')} type="button">
            <ArrowLeft size={14} /> Projects
          </button>
          <button className="btn btn-secondary" onClick={() => { setAiOpen(true); setAiSuggestions([]); setAiError('') }} type="button">
            <Sparkles size={15} /> Suggest with AI
          </button>
          <button className="btn btn-secondary" onClick={openTeam} type="button">
            <Users size={15} /> Team
          </button>
          <button className="btn btn-primary" onClick={openCreate} type="button" id="new-issue-btn">
            <Plus size={16} /> New issue
          </button>
        </>
      }
    >
      {/* Toolbar */}
      <div className="toolbar">
        <div className="search-input-wrap">
          <Search size={15} />
          <input
            type="search"
            placeholder="Search issues..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPageIndex(0) }}
            id="issue-search"
          />
        </div>
        <SlidersHorizontal size={15} style={{ color: 'var(--text-muted)', flexShrink: 0 }} />
        <select
          className="filter-select"
          value={filterStatus}
          onChange={(e) => { setFilterStatus(e.target.value as IssueStatus | ''); setPageIndex(0) }}
          id="filter-status"
        >
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
        </select>
        <select
          className="filter-select"
          value={filterPriority}
          onChange={(e) => { setFilterPriority(e.target.value as IssuePriority | ''); setPageIndex(0) }}
          id="filter-priority"
        >
          <option value="">All priorities</option>
          {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>

      <div style={{ marginBottom: 16 }}>
        <button className="btn btn-ghost btn-sm" type="button" onClick={() => setShowActivity((visible) => !visible)} aria-expanded={showActivity}>
          <History size={14} /> {showActivity ? 'Hide activity' : 'Recent activity'}
        </button>
        {showActivity && (
          <div className="card" style={{ marginTop: 10 }}>
            {activity.length === 0 ? <p>No project activity yet.</p> : activity.map((event) => (
              <div key={event.id} className="detail-field" style={{ borderBottom: '1px solid var(--border)', padding: '10px 0' }}>
                <div className="detail-field-value">{event.summary}</div>
                <div className="detail-field-label">{event.actorEmail} · {new Date(event.createdAt).toLocaleString()}</div>
              </div>
            ))}
          </div>
        )}
      </div>

      {loading && <div className="loading-center"><div className="spinner" /><span>Loading issues...</span></div>}

      {error && !loading && (
        <div className="alert alert-error" role="alert">
          {error}
          <button className="btn btn-ghost btn-sm" onClick={load} type="button" style={{ marginLeft: 'auto' }}>Retry</button>
        </div>
      )}

      {!loading && !error && issues.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon"><Bug size={32} /></div>
          <h3>{search || filterStatus || filterPriority ? 'No matching issues' : 'No issues yet'}</h3>
          <p>{search || filterStatus || filterPriority ? 'Try adjusting your filters.' : 'Track bugs, tasks, and improvements.'}</p>
          {!search && !filterStatus && !filterPriority && (
            <button className="btn btn-primary" onClick={openCreate} type="button">
              <Plus size={15} /> Create issue
            </button>
          )}
        </div>
      )}

      {!loading && !error && issues.length > 0 && (
        <div className="issue-list">
          {issues.map((issue) => (
            <div key={issue.id} className="issue-card" onClick={() => openView(issue)} role="button" tabIndex={0}>
              <div className={`issue-priority-dot dot-${issue.priority.toLowerCase()}`} />
              <div className="issue-info">
                <div className="issue-title">{issue.title}</div>
                <div className="issue-meta">
                  <span className={`badge ${priorityClass(issue.priority)}`}>{issue.priority}</span>
                  {issue.dueDate && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                      <CalendarDays size={11} /> {issue.dueDate}
                    </span>
                  )}
                </div>
              </div>
              <span className={`badge ${statusClass(issue.status)}`}>{issue.status.replace('_', ' ')}</span>
              <div style={{ display: 'flex', gap: 6 }}>
                <button className="btn btn-ghost btn-icon btn-sm" onClick={(e) => openEdit(issue, e)} aria-label="Edit" title="Edit" type="button">
                  <Pencil size={13} />
                </button>
                <button className="btn btn-ghost btn-icon btn-sm" onClick={(e) => openDelete(issue, e)} aria-label="Delete" title="Delete" type="button" style={{ color: 'var(--danger)' }}>
                  <Trash2 size={13} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && !error && issueTotalPages > 1 && (
        <div className="flex items-center justify-between" style={{ marginTop: 18 }}>
          <span className="text-sm text-muted">Page {pageIndex + 1} of {issueTotalPages}</span>
          <div className="flex gap-2">
            <button className="btn btn-secondary btn-sm" type="button" disabled={pageIndex === 0} onClick={() => setPageIndex((page) => Math.max(0, page - 1))}>Previous</button>
            <button className="btn btn-secondary btn-sm" type="button" disabled={pageIndex + 1 >= issueTotalPages} onClick={() => setPageIndex((page) => Math.min(issueTotalPages - 1, page + 1))}>Next</button>
          </div>
        </div>
      )}

      {/* Create */}
      {teamOpen && project && (
        <Modal title="Project team" onClose={() => setTeamOpen(false)}>
          {teamError && <div className="alert alert-error" role="alert" style={{ marginBottom: 12 }}>{teamError}</div>}
          <div className="issue-list" style={{ marginBottom: 18 }}>
            <div className="issue-card"><div className="issue-info"><strong>{project.ownerEmail === user?.email ? user?.name : project.ownerEmail}</strong><div className="issue-meta">{project.ownerEmail}</div></div><span className="badge badge-active">OWNER</span></div>
            {team.map((member) => <div className="issue-card" key={member.userId}>
              <div className="issue-info"><strong>{member.name}</strong><div className="issue-meta">{member.email}</div></div>
              {project.ownerEmail === user?.email ? <select aria-label={`Role for ${member.email}`} value={member.role} onChange={(e) => changeMemberRole(member, e.target.value as ProjectRole)} style={{ width: 120 }}>
                {(['ADMIN', 'MEMBER', 'VIEWER'] as ProjectRole[]).map((role) => <option key={role}>{role}</option>)}
              </select> : <span className="badge badge-open">{member.role}</span>}
              {project.ownerEmail === user?.email && <button className="btn btn-ghost btn-sm" type="button" onClick={() => deleteMember(member)}>Remove</button>}
            </div>)}
          </div>
          {project.ownerEmail === user?.email && <form onSubmit={inviteMember}>
            <div className="form-row">
              <div className="form-group"><label htmlFor="member-email">Registered user email</label><input id="member-email" type="email" required value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} /></div>
              <div className="form-group"><label htmlFor="member-role">Role</label><select id="member-role" value={memberRole} onChange={(e) => setMemberRole(e.target.value as ProjectRole)}>{(['ADMIN', 'MEMBER', 'VIEWER'] as ProjectRole[]).map((role) => <option key={role}>{role}</option>)}</select></div>
            </div>
            <button className="btn btn-primary" type="submit">Add member</button>
          </form>}
        </Modal>
      )}

      {aiOpen && (
        <Modal title="AI issue suggestions" onClose={() => setAiOpen(false)}>
          <form onSubmit={generateSuggestions}>
            <p style={{ marginBottom: 14 }}>Describe a feature or goal. Review each suggestion before adding it to this project.</p>
            <div className="form-group">
              <label htmlFor="ai-context">Feature or goal</label>
              <textarea id="ai-context" value={aiContext} onChange={(e) => setAiContext(e.target.value)} rows={3} maxLength={10000} placeholder="For example: add password reset by email" />
            </div>
            {aiError && <div className="alert alert-error" role="alert" style={{ marginBottom: 14 }}>{aiError}</div>}
            <div className="modal-actions">
              <button className="btn btn-secondary" type="button" onClick={() => setAiOpen(false)}>Close</button>
              <button className="btn btn-primary" type="submit" disabled={aiLoading || !aiContext.trim()}>
                {aiLoading ? <><div className="spinner" /> Generating...</> : <><Sparkles size={14} /> Generate suggestions</>}
              </button>
            </div>
          </form>
          {aiSuggestions.map((suggestion, index) => (
            <article className="card" key={`${suggestion.title}-${index}`} style={{ marginTop: 12 }}>
              <div className="flex justify-between items-center gap-2">
                <strong>{suggestion.title}</strong>
                <span className={`badge ${priorityClass(suggestion.priority)}`}>{suggestion.priority}</span>
              </div>
              <p style={{ margin: '8px 0 12px' }}>{suggestion.description}</p>
              <button className="btn btn-secondary btn-sm" type="button" onClick={() => addSuggestion(suggestion, index)} disabled={aiSaving !== null}>
                {aiSaving === index ? 'Adding...' : 'Add issue'}
              </button>
            </article>
          ))}
        </Modal>
      )}

      {modal === 'create' && <Modal title="New issue" onClose={() => setModal(null)}>{renderIssueForm(handleCreate)}</Modal>}

      {/* Edit */}
      {modal === 'edit' && selected && <Modal title="Edit issue" onClose={() => setModal(null)}>{renderIssueForm(handleEdit)}</Modal>}

      {/* View Detail */}
      {modal === 'view' && selected && (
        <Modal title="Issue detail" onClose={() => setModal(null)}>
          <div className="detail-field">
            <span className="detail-field-label">Title</span>
            <div className="detail-field-value" style={{ fontSize: 16, fontWeight: 600 }}>{selected.title}</div>
          </div>
          {selected.description && (
            <div className="detail-field">
              <span className="detail-field-label">Description</span>
              <div className="detail-field-value" style={{ whiteSpace: 'pre-wrap' }}>{selected.description}</div>
            </div>
          )}
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', margin: '16px 0' }}>
            <div className="detail-field" style={{ flex: 1 }}>
              <span className="detail-field-label">Status</span>
              <span className={`badge ${statusClass(selected.status)}`}>{selected.status.replace('_', ' ')}</span>
            </div>
            <div className="detail-field" style={{ flex: 1 }}>
              <span className="detail-field-label">Priority</span>
              <span className={`badge ${priorityClass(selected.priority)}`}>{selected.priority}</span>
            </div>
          </div>
          {selected.dueDate && (
            <div className="detail-field">
              <span className="detail-field-label">Due Date</span>
              <div className="detail-field-value">{selected.dueDate}</div>
            </div>
          )}
          <div className="detail-field">
            <span className="detail-field-label">Created</span>
            <div className="detail-field-value">{new Date(selected.createdAt).toLocaleString()}</div>
          </div>
          <div className="modal-actions">
            <button className="btn btn-secondary" onClick={() => setModal(null)} type="button">Close</button>
            <button className="btn btn-primary" onClick={() => { openEdit(selected, { stopPropagation: () => {} } as React.MouseEvent) }} type="button">
              <Pencil size={14} /> Edit
            </button>
          </div>
        </Modal>
      )}

      {/* Delete confirm */}
      {modal === 'delete' && selected && (
        <Modal title="Delete issue" onClose={() => setModal(null)}>
          <div className="alert alert-error" style={{ marginBottom: 20 }}>
            This will permanently delete <strong>{selected.title}</strong>.
          </div>
          {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
          <div className="modal-actions">
            <button className="btn btn-secondary" onClick={() => setModal(null)} type="button">Cancel</button>
            <button className="btn btn-danger" onClick={handleDelete} disabled={saving} type="button">
              {saving ? <><div className="spinner" /> Deleting...</> : 'Delete issue'}
            </button>
          </div>
        </Modal>
      )}
    </AppLayout>
  )
}
