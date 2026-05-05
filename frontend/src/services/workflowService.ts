import { api } from './api'

export interface WorkflowTaskApi {
  id: string
  name?: string
  taskDefinitionKey?: string
  assignee?: string
  processInstanceId?: string
  dossierId?: number
  created?: string
}

export const workflowService = {
  async start(dossierId: number | string) {
    const { data } = await api.post(`/api/workflow/start/${dossierId}`)
    return data
  },

  async listTasks(params?: { assignee?: string; candidateGroup?: string; dossierId?: number | string }) {
    const { data } = await api.get<WorkflowTaskApi[]>('/api/workflow/tasks', { params })
    return data
  },

  async completeTask(taskId: string, variables?: Record<string, any>) {
    const { data } = await api.post(`/api/workflow/tasks/${taskId}/complete`, {
      variables: variables || {},
    })
    return data
  },
}

