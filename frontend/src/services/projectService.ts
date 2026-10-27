
import { apiRequest } from './api'

export type Project = {
  id: number
  name: string
  description: string
  status: 'ACTIVE' | 'ARCHIVED'
  createdAt: string
  updatedAt?: string
  ownerEmail?: string
}

export type ProjectPage = {
  content: Project[]
  page?: { totalElements: number; totalPages: number; number: number; size: number }
}

export type CreateProjectRequest = {
  name: string
  description: string
}

export async function getProjects(token: string, page = 0, size = 50): Promise<ProjectPage> {
  return apiRequest<ProjectPage>(`/api/projects?page=${page}&size=${size}&sort=createdAt,desc`, {}, token)
}

export async function getProject(token: string, id: number): Promise<Project> {
  return apiRequest<Project>(`/api/projects/${id}`, {}, token)
}

export async function createProject(token: string, data: CreateProjectRequest): Promise<Project> {
  return apiRequest<Project>('/api/projects', { method: 'POST', body: JSON.stringify(data) }, token)
}

export async function updateProject(token: string, id: number, data: CreateProjectRequest): Promise<Project> {
  return apiRequest<Project>(`/api/projects/${id}`, { method: 'PUT', body: JSON.stringify(data) }, token)
}

export async function setProjectStatus(token: string, id: number, status: Project['status']): Promise<Project> {
  return apiRequest<Project>(`/api/projects/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }, token)
}

export async function deleteProject(token: string, id: number): Promise<void> {
  return apiRequest<void>(`/api/projects/${id}`, { method: 'DELETE' }, token)
}
