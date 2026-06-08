import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
import type {
  AuthUser,
  OntologyListItem,
  OntologyPageResponse,
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
  return useMutation({
    mutationFn: async (file: File) => {
      const form = new FormData()
      form.append('file', file)
      const { data } = await api.post<{ success: boolean; message: string }>(
        '/upload-asta',
        form,
      )
      return data
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
