import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type {
  AstaVersion,
  AuthUser,
  NewPublication,
  OntologyListItem,
  OntologyPageResponse,
  Publication,
} from '../types/api'

export function useOntologyList() {
  return useQuery({
    queryKey: ['ontologies'],
    queryFn: async () => {
      const { data } = await api.get<OntologyListItem[]>('/api/ontologies')
      return data
    },
  })
}

export function useOntology(name: string | undefined) {
  return useQuery({
    queryKey: ['ontology', name],
    enabled: !!name,
    queryFn: async () => {
      const { data } = await api.get<OntologyPageResponse>(
        `/api/ontologies/${name}`,
      )
      return data
    },
  })
}

export function usePublications() {
  return useQuery({
    queryKey: ['publications'],
    queryFn: async () => {
      const { data } = await api.get<Publication[]>('/api/publications')
      return data
    },
  })
}

export function useAddPublication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (publication: NewPublication) => {
      const { data } = await api.post<Publication>(
        '/api/publications',
        publication,
      )
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['publications'] })
    },
  })
}

export function useUpdatePublication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (vars: { id: string; publication: NewPublication }) => {
      const { data } = await api.put<Publication>(
        `/api/publications/${vars.id}`,
        vars.publication,
      )
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['publications'] })
    },
  })
}

export function useDeletePublication() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/publications/${id}`)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['publications'] })
    },
  })
}

export function useCurrentUser() {
  return useQuery({
    queryKey: ['auth', 'me'],
    queryFn: async () => {
      try {
        const { data } = await api.get<AuthUser>('/api/auth/me')
        return data
      } catch (err: unknown) {
        if (axiosIsUnauthorized(err)) return null
        throw err
      }
    },
    retry: false,
  })
}

export function useLogin() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (vars: { username: string; password: string }) => {
      const body = new URLSearchParams()
      body.set('username', vars.username)
      body.set('password', vars.password)
      const { data } = await api.post<AuthUser>('/login', body, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      return data
    },
    onSuccess: (user) => {
      qc.setQueryData(['auth', 'me'], user)
    },
  })
}

export function useLogout() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async () => {
      await api.post('/logout')
    },
    onSuccess: () => {
      qc.setQueryData(['auth', 'me'], null)
    },
  })
}

export function useUploadAsta() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (vars: { file: File; note?: string }) => {
      const form = new FormData()
      form.append('file', vars.file)
      if (vars.note) form.append('note', vars.note)
      const { data } = await api.post<{ success: boolean; message: string }>(
        '/upload-asta',
        form,
      )
      return data
    },
    onSuccess: () => {
      // A successful upload becomes the active version and reloads the model.
      qc.invalidateQueries({ queryKey: ['asta-versions'] })
      qc.invalidateQueries({ queryKey: ['ontologies'] })
    },
  })
}

/** Admin-only history of uploaded .asta files, newest first. */
export function useAstaVersions(enabled: boolean) {
  return useQuery({
    queryKey: ['asta-versions'],
    enabled,
    queryFn: async () => {
      const { data } = await api.get<AstaVersion[]>('/api/asta/versions')
      return data
    },
  })
}

/** Rolls the site back to an archived version: re-parses it and regenerates the diagrams. */
export function useActivateAstaVersion() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      const { data } = await api.post<AstaVersion>(
        `/api/asta/versions/${id}/activate`,
      )
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['asta-versions'] })
      qc.invalidateQueries({ queryKey: ['ontologies'] })
      qc.invalidateQueries({ queryKey: ['ontology'] })
    },
  })
}

export function useUpdateAstaVersionNote() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (vars: { id: string; note: string }) => {
      const { data } = await api.patch<AstaVersion>(
        `/api/asta/versions/${vars.id}`,
        { note: vars.note },
      )
      return data
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['asta-versions'] })
    },
  })
}

export function useDeleteAstaVersion() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/asta/versions/${id}`)
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['asta-versions'] })
    },
  })
}

/**
 * Downloads an archived .asta through the API (not a plain link), so the request carries the
 * session cookie and the same base URL as every other call.
 */
export function useDownloadAstaVersion() {
  return useMutation({
    mutationFn: async (vars: { id: string; filename: string }) => {
      const { data } = await api.get<Blob>(
        `/api/asta/versions/${vars.id}/download`,
        { responseType: 'blob' },
      )
      const url = URL.createObjectURL(data)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = vars.filename
      anchor.click()
      URL.revokeObjectURL(url)
    },
  })
}

function axiosIsUnauthorized(err: unknown): boolean {
  return (
    typeof err === 'object' &&
    err !== null &&
    'response' in err &&
    (err as { response?: { status?: number } }).response?.status === 401
  )
}
