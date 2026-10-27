import { apiRequest } from './api'
import type { IssuePriority, IssueStatus } from './issueService'

export type IssueSuggestion = {
  title: string
  description: string
  priority: IssuePriority
  status: IssueStatus
}

export async function suggestIssues(
  token: string,
  projectId: number,
  context: string,
  count = 5,
): Promise<{ issues: IssueSuggestion[]; model: string }> {
  return apiRequest<{ issues: IssueSuggestion[]; model: string }>(`/api/projects/${projectId}/ai/suggest-issues`, {
    method: 'POST',
    body: JSON.stringify({ context, count }),
  }, token)
}
