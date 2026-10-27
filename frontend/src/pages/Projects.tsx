import { useEffect, useState, useCallback, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, FolderKanban, Search, Pencil, Trash2, CalendarDays, ArchiveRestore } from 'lucide-react'
import AppLayout from '../layouts/AppLayout'
import Modal from '../components/Modal'
import { useAuth } from '../contexts/AuthContext'
import {
  getProjects,
  createProject,
  updateProject,
  deleteProject,
  setProjectStatus,
  type Project,
} from '../services/projectService'

export default function ProjectsPage() {
  const { token, user } = useAuth()
  const navigate = useNavigate()

  const [projects, setProjects] = useState<Project[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')

  // Modal state
  const [modal, setModal] = useState<'create' | 'edit' | 'delete' | null>(null)
  const [selected, setSelected] = useState<Project | null>(null)
  const [formName, setFormName] = useState('')
  const [formDesc, setFormDesc] = useState('')
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const load = useCallback(async () => {
    if (!token) return
    setLoading(true)
    setError('')
    try {
      const page = await getProjects(token, 0, 100)
      setProjects(page.content)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load projects.')
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
    const q = search.toLowerCase()
    return projects.filter((p) => p.name.toLowerCase().includes(q) || (p.description ?? '').toLowerCase().includes(q))
  }, [search, projects])

  function openCreate() {
    setFormName('')
    setFormDesc('')
    setFormError('')
    setModal('create')
  }

  function openEdit(p: Project, e: React.MouseEvent) {
    e.stopPropagation()
    setSelected(p)
    setFormName(p.name)
    setFormDesc(p.description ?? '')
    setFormError('')
    setModal('edit')
  }

  function openDelete(p: Project, e: React.MouseEvent) {
    e.stopPropagation()
    setSelected(p)
    setModal('delete')
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    if (!token) return
    setSaving(true)
    setFormError('')
    try {
      await createProject(token, { name: formName.trim(), description: formDesc.trim() })
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to create project.')
    } finally {
      setSaving(false)
    }
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault()
    if (!token || !selected) return
    setSaving(true)
    setFormError('')
    try {
      await updateProject(token, selected.id, { name: formName.trim(), description: formDesc.trim() })
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to update project.')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!token || !selected) return
    setSaving(true)
    try {
      await deleteProject(token, selected.id)
      setModal(null)
      load()
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'Failed to delete project.')
    } finally {
      setSaving(false)
    }
  }

  async function toggleArchive(project: Project, e: React.MouseEvent) {
    e.stopPropagation()
    if (!token) return
    try {
      await setProjectStatus(token, project.id, project.status === 'ACTIVE' ? 'ARCHIVED' : 'ACTIVE')
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not update project status.')
    }
  }

  const renderProjectForm = (onSubmit: (e: React.FormEvent) => void) => (
    <form onSubmit={onSubmit}>
      {formError && <div className="alert alert-error" style={{ marginBottom: 18 }}>{formError}</div>}
      <div className="form-group">
        <label htmlFor="proj-name">Project name <span style={{ color: 'var(--danger)' }}>*</span></label>
        <input
          id="proj-name"
          type="text"
          value={formName}
          onChange={(e) => setFormName(e.target.value)}
          placeholder="e.g. Backend API v2"
          required
          maxLength={120}
          autoFocus
        />
      </div>
      <div className="form-group">
        <label htmlFor="proj-desc">Description</label>
        <textarea
          id="proj-desc"
          value={formDesc}
          onChange={(e) => setFormDesc(e.target.value)}
          placeholder="What is this project about?"
          rows={3}
          style={{ resize: 'vertical' }}
        />
      </div>
      <div className="modal-actions">
        <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving || !formName.trim()}>
          {saving ? <><div className="spinner" /> Saving...</> : modal === 'create' ? 'Create project' : 'Save changes'}
        </button>
      </div>
    </form>
  )

  return (
    <AppLayout
      title="Projects"
      subtitle="Organize your work into projects."
      actions={
        <button className="btn btn-primary" onClick={openCreate} type="button" id="new-project-btn">
          <Plus size={16} /> New project
        </button>
      }
    >
      {/* Toolbar */}
      <div className="toolbar">
        <div className="search-input-wrap">
          <Search size={15} />
          <input
            type="search"
            placeholder="Search projects..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            id="project-search"
          />
        </div>
      </div>

      {/* Content */}
      {loading && (
        <div className="loading-center">
          <div className="spinner" />
          <span>Loading projects...</span>
        </div>
      )}

      {error && !loading && (
        <div className="alert alert-error" role="alert">
          {error}
          <button className="btn btn-ghost btn-sm" onClick={load} type="button" style={{ marginLeft: 'auto' }}>
            Retry
          </button>
        </div>
      )}

      {!loading && !error && filtered.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon">
            <FolderKanban size={32} />
          </div>
          <h3>{search ? 'No matching projects' : 'No projects yet'}</h3>
          <p>
            {search
              ? 'Try a different search term.'
              : 'Create your first project to start tracking issues and progress.'}
          </p>
          {!search && (
            <button className="btn btn-primary" onClick={openCreate} type="button">
              <Plus size={15} /> Create project
            </button>
          )}
        </div>
      )}

      {!loading && !error && filtered.length > 0 && (
        <div className="project-grid">
          {filtered.map((p) => (
            <article
              key={p.id}
              className="project-card"
              onClick={() => navigate(`/projects/${p.id}/issues`)}
              tabIndex={0}
              role="button"
              onKeyDown={(e) => e.key === 'Enter' && navigate(`/projects/${p.id}/issues`)}
              aria-label={`Open project ${p.name}`}
            >
              <div className="project-card-header">
                <div className="project-name">{p.name}</div>
                <span className={`badge badge-${p.status.toLowerCase()}`}>{p.status}</span>
              </div>
              <div className="project-description">{p.description || 'No description provided.'}</div>
              <div className="project-card-footer">
                <div className="project-meta">
                  <CalendarDays size={12} />
                  {new Date(p.createdAt).toLocaleDateString()}
                </div>
                <div style={{ display: 'flex', gap: 6 }}>
                  <button
                  className="btn btn-ghost btn-icon btn-sm"
                  onClick={(e) => toggleArchive(p, e)}
                  aria-label={p.status === 'ACTIVE' ? 'Archive project' : 'Restore project'}
                  title={p.status === 'ACTIVE' ? 'Archive' : 'Restore'}
                  type="button"
                  disabled={p.ownerEmail !== user?.email}
                >
                  <ArchiveRestore size={14} />
                </button>
                <button
                    className="btn btn-ghost btn-icon btn-sm"
                    onClick={(e) => openEdit(p, e)}
                    aria-label="Edit project"
                    title="Edit"
                    type="button"
                  disabled={p.ownerEmail !== user?.email}
                  >
                    <Pencil size={13} />
                  </button>
                  <button
                    className="btn btn-ghost btn-icon btn-sm"
                    onClick={(e) => openDelete(p, e)}
                    aria-label="Delete project"
                    title="Delete"
                    type="button"
                    disabled={p.ownerEmail !== user?.email}
                    style={{ color: 'var(--danger)' }}
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

      {/* Create modal */}
      {modal === 'create' && (
        <Modal title="New project" onClose={() => setModal(null)}>
          {renderProjectForm(handleCreate)}
        </Modal>
      )}

      {/* Edit modal */}
      {modal === 'edit' && selected && (
        <Modal title="Edit project" onClose={() => setModal(null)}>
          {renderProjectForm(handleEdit)}
        </Modal>
      )}

      {/* Delete confirm */}
      {modal === 'delete' && selected && (
        <Modal title="Delete project" onClose={() => setModal(null)}>
          <div className="alert alert-error" style={{ marginBottom: 20 }}>
            This will permanently delete <strong>{selected.name}</strong> and all its issues.
          </div>
          {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
          <div className="modal-actions">
            <button className="btn btn-secondary" onClick={() => setModal(null)} type="button">
              Cancel
            </button>
            <button className="btn btn-danger" onClick={handleDelete} disabled={saving} type="button">
              {saving ? <><div className="spinner" /> Deleting...</> : 'Delete project'}
            </button>
          </div>
        </Modal>
      )}
    </AppLayout>
  )
}
