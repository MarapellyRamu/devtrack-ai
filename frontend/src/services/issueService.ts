
import { apiRequest } from './api'

export type IssuePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type IssueStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'

export type Issue = {
  id: number
  projectId: number
  title: string
  description: string
  priority: IssuePriority
  status: IssueStatus
  dueDate?: string
  createdAt: string
  updatedAt?: string
}

export type IssuePage = {
  content: Issue[]
  page?: { totalElements: number; totalPages: number; number: number; size: number }
}

export type IssueRequest = {
  title: string
  description: string
  priority: IssuePriority
  status: IssueStatus
  dueDate?: string
}

export async function getProjectIssues(
  token: string,
  projectId: number,
  page = 0,
  size = 50,
  status?: IssueStatus,
  priority?: IssuePriority,
  search?: string,
): Promise<IssuePage> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
    sort: 'createdAt,desc',
  })
  if (status) params.append('status', status)
  if (priority) params.append('priority', priority)
  if (search?.trim()) params.append('search', search.trim())
  return apiRequest<IssuePage>(`/api/projects/${projectId}/issues?${params}`, {}, token)
}

export async function getIssue(token: string, projectId: number, issueId: number): Promise<Issue> {
  return apiRequest<Issue>(`/api/projects/${projectId}/issues/${issueId}`, {}, token)
}

export async function createIssue(token: string, projectId: number, data: IssueRequest): Promise<Issue> {
  return apiRequest<Issue>(`/api/projects/${projectId}/issues`, {
    method: 'POST',
    body: JSON.stringify(data),
  }, token)
}

export async function updateIssue(token: string, projectId: number, issueId: number, data: IssueRequest): Promise<Issue> {
  return apiRequest<Issue>(`/api/projects/${projectId}/issues/${issueId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }, token)
}

export async function deleteIssue(token: string, projectId: number, issueId: number): Promise<void> {
  return apiRequest<void>(`/api/projects/${projectId}/issues/${issueId}`, { method: 'DELETE' }, token)
}
