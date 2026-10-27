import { apiRequest } from './api'

export type ActivityEvent = {
  id: number
  projectId: number
  actorEmail: string
  action: string
  itemType: string
  itemId: number
  summary: string
  createdAt: string
}

export async function getProjectActivity(token: string, projectId: number): Promise<{ content: ActivityEvent[] }> {
  return apiRequest<{ content: ActivityEvent[] }>(`/api/projects/${projectId}/activity?page=0&size=20&sort=createdAt,desc`, {}, token)
}
