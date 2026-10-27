import { apiRequest } from './api'

export type NotificationItem = {
  id: number
  projectId: number
  summary: string
  actorEmail: string
  action: string
  itemType: string
  itemId: number
  createdAt: string
  readAt?: string
}
export type NotificationPage = { content: NotificationItem[]; page?: { totalElements: number } }

export function getNotifications(token: string): Promise<NotificationPage> {
  return apiRequest<NotificationPage>('/api/notifications?page=0&size=50&sort=createdAt,desc', {}, token)
}
export function getUnreadNotificationCount(token: string): Promise<{ count: number }> {
  return apiRequest<{ count: number }>('/api/notifications/unread-count', {}, token)
}
export function markNotificationRead(token: string, notificationId: number): Promise<void> {
  return apiRequest<void>(`/api/notifications/${notificationId}/read`, { method: 'PATCH' }, token)
}
