import { apiRequest } from './api'

export type ProjectRole = 'ADMIN' | 'MEMBER' | 'VIEWER'
export type ProjectMember = { id: number; userId: number; name: string; email: string; role: ProjectRole; createdAt: string }

export async function getMembers(token: string, projectId: number): Promise<ProjectMember[]> {
  return apiRequest<ProjectMember[]>(`/api/projects/${projectId}/members`, {}, token)
}
export async function addMember(token: string, projectId: number, email: string, role: ProjectRole): Promise<ProjectMember> {
  return apiRequest<ProjectMember>(`/api/projects/${projectId}/members`, { method: 'POST', body: JSON.stringify({ email, role }) }, token)
}
export async function updateMemberRole(token: string, projectId: number, userId: number, role: ProjectRole): Promise<ProjectMember> {
  return apiRequest<ProjectMember>(`/api/projects/${projectId}/members/${userId}`, { method: 'PUT', body: JSON.stringify({ role }) }, token)
}
export async function removeMember(token: string, projectId: number, userId: number): Promise<void> {
  return apiRequest(`/api/projects/${projectId}/members/${userId}`, { method: 'DELETE' }, token)
}
