import {
  Alert,
  Badge,
  Box,
  Button,
  CloseButton,
  Dialog,
  Field,
  Heading,
  HStack,
  Input,
  Portal,
  Spinner,
  Stack,
  Table,
  Text,
  VStack,
} from '@chakra-ui/react'
import { useRef, useState, type ChangeEvent, type DragEvent, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import {
  useActivateAstaVersion,
  useAstaVersions,
  useCurrentUser,
  useDeleteAstaVersion,
  useDownloadAstaVersion,
  useUpdateAstaVersionNote,
  useUploadAsta,
} from '../api/queries'
import type { AstaVersion } from '../types/api'

export default function UploadPage() {
  const { data: user, isLoading } = useCurrentUser()
  const upload = useUploadAsta()
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [file, setFile] = useState<File | null>(null)
  const [note, setNote] = useState('')
  const [dragOver, setDragOver] = useState(false)

  if (isLoading) return <Text>Loading…</Text>
  if (!user) return <Navigate to="/login" replace />

  function onFileSelected(e: ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0] ?? null
    setFile(f)
  }

  function onDrop(e: DragEvent<HTMLDivElement>) {
    e.preventDefault()
    setDragOver(false)
    const f = e.dataTransfer.files?.[0] ?? null
    if (f && f.name.endsWith('.asta')) setFile(f)
  }

  function onUpload() {
    if (!file) return
    upload.mutate(
      { file, note: note.trim() || undefined },
      { onSuccess: () => setNote('') },
    )
  }

  return (
    <VStack align="stretch" gap={6}>
      <Box bg="bg.subtle" p={8} borderRadius="md">
        <Heading size="2xl">Upload Astah File</Heading>
        <Text fontSize="lg" mt={2}>Upload your .asta file to generate SEON Pages</Text>
      </Box>

      <Box
        borderWidth="2px"
        borderStyle="dashed"
        borderColor={dragOver ? 'green.500' : 'border'}
        borderRadius="md"
        p={10}
        textAlign="center"
        cursor="pointer"
        bg={dragOver ? 'green.50' : 'bg'}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault()
          setDragOver(true)
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDrop}
      >
        <Heading size="md" mb={2}>Drag and drop your .asta file here</Heading>
        <Text color="fg.muted" mb={4}>or click to select a file</Text>
        <Input
          ref={inputRef}
          type="file"
          accept=".asta"
          display="none"
          onChange={onFileSelected}
        />
        <Button variant="outline">Choose File</Button>
      </Box>

      {file && (
        <Alert.Root status="info">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>Selected file</Alert.Title>
            <Alert.Description>{file.name}</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      <Field.Root>
        <Field.Label>Note (optional)</Field.Label>
        <Input
          value={note}
          maxLength={200}
          placeholder="e.g. version presented at SBES 2026"
          onChange={(e) => setNote(e.target.value)}
        />
        <Field.HelperText>
          Shown in the version history below, to tell this upload from the others.
        </Field.HelperText>
      </Field.Root>

      <Button
        colorPalette="green"
        size="lg"
        disabled={!file || upload.isPending}
        loading={upload.isPending}
        loadingText="Uploading…"
        onClick={onUpload}
      >
        Upload and Process
      </Button>

      {upload.isSuccess && upload.data && (
        <Alert.Root status={upload.data.success ? 'success' : 'error'}>
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>{upload.data.success ? 'Done' : 'Failed'}</Alert.Title>
            <Alert.Description>{upload.data.message}</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      {upload.isError && (
        <Alert.Root status="error">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>Upload failed</Alert.Title>
            <Alert.Description>{errorMessage(upload.error)}</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      <VersionHistory />
    </VStack>
  )
}

/** Admin-only list of every archived .asta, with rollback, download and delete. */
function VersionHistory() {
  const { data: versions, isLoading, isError } = useAstaVersions(true)
  const download = useDownloadAstaVersion()

  return (
    <Box mt={4}>
      <Heading size="lg" mb={1}>Version history</Heading>
      <Text color="fg.muted" mb={4}>
        Every upload is archived here. Only admins can see this list. Activating an older version
        re-reads it, regenerates the diagrams and puts it back on the site.
      </Text>

      {isLoading && <Spinner />}

      {isError && (
        <Alert.Root status="error">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>Could not load the version history.</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      {versions && versions.length === 0 && (
        <Text color="fg.muted">No versions recorded yet. The next upload starts the history.</Text>
      )}

      {versions && versions.length > 0 && (
        <Table.Root size="md" interactive>
          <Table.Header>
            <Table.Row>
              <Table.ColumnHeader width="170px">Uploaded</Table.ColumnHeader>
              <Table.ColumnHeader>File</Table.ColumnHeader>
              <Table.ColumnHeader width="150px">Model</Table.ColumnHeader>
              <Table.ColumnHeader width="230px">Actions</Table.ColumnHeader>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {versions.map((version) => (
              <Table.Row key={version.id}>
                <Table.Cell>
                  <Text>{formatDate(version.uploadedAt)}</Text>
                  <Text fontSize="xs" color="fg.muted">by {version.uploadedBy}</Text>
                </Table.Cell>
                <Table.Cell>
                  <HStack gap={2} mb={1}>
                    <Text fontWeight="bold">{version.originalFilename ?? 'astah_seon.asta'}</Text>
                    {version.active && <Badge colorPalette="green">CURRENT</Badge>}
                  </HStack>
                  <Text fontSize="sm" color="fg.muted">
                    {version.note ?? '—'}
                  </Text>
                  <Text fontSize="xs" color="fg.muted">
                    {formatSize(version.sizeBytes)} · sha256 {version.sha256.slice(0, 12)}…
                  </Text>
                </Table.Cell>
                <Table.Cell>
                  <Text fontSize="sm">{version.ontologyCount} ontologies</Text>
                  <Text fontSize="sm" color="fg.muted">{version.conceptCount} concepts</Text>
                </Table.Cell>
                <Table.Cell>
                  <Stack gap={2}>
                    <HStack gap={2}>
                      {!version.active && <ActivateVersionDialog version={version} />}
                      <Button
                        size="xs"
                        variant="outline"
                        loading={download.isPending && download.variables?.id === version.id}
                        onClick={() =>
                          download.mutate({
                            id: version.id,
                            filename: version.originalFilename ?? 'astah_seon.asta',
                          })
                        }
                      >
                        Download
                      </Button>
                    </HStack>
                    <HStack gap={2}>
                      <EditNoteDialog version={version} />
                      {!version.active && <DeleteVersionDialog version={version} />}
                    </HStack>
                  </Stack>
                </Table.Cell>
              </Table.Row>
            ))}
          </Table.Body>
        </Table.Root>
      )}

      {download.isError && (
        <Alert.Root status="error" mt={4}>
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Description>{errorMessage(download.error)}</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}
    </Box>
  )
}

/** Rollback is a live change to the whole site, so it is confirmed first. */
function ActivateVersionDialog({ version }: { version: AstaVersion }) {
  const [open, setOpen] = useState(false)
  const activate = useActivateAstaVersion()

  function onOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    activate.reset()
  }

  return (
    <Dialog.Root open={open} onOpenChange={(e) => onOpenChange(e.open)} role="alertdialog">
      <Dialog.Trigger asChild>
        <Button size="xs" colorPalette="green">Activate</Button>
      </Dialog.Trigger>
      <Portal>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.Header>
              <Dialog.Title>Activate this version</Dialog.Title>
            </Dialog.Header>
            <Dialog.Body>
              <Stack gap={4}>
                {activate.isError && (
                  <Alert.Root status="error">
                    <Alert.Indicator />
                    <Alert.Content>
                      <Alert.Description>{errorMessage(activate.error)}</Alert.Description>
                    </Alert.Content>
                  </Alert.Root>
                )}
                <Text>
                  The site will serve the file uploaded on {formatDate(version.uploadedAt)} again.
                  All diagrams are regenerated, which takes a moment.
                </Text>
                <Box borderWidth="1px" borderRadius="md" p={3} bg="bg.subtle">
                  <Text fontWeight="bold">{version.originalFilename ?? 'astah_seon.asta'}</Text>
                  <Text fontSize="sm" color="fg.muted">{version.note ?? 'No note'}</Text>
                </Box>
              </Stack>
            </Dialog.Body>
            <Dialog.Footer>
              <Dialog.ActionTrigger asChild>
                <Button variant="outline">Cancel</Button>
              </Dialog.ActionTrigger>
              <Button
                colorPalette="green"
                loading={activate.isPending}
                loadingText="Activating…"
                onClick={() => activate.mutate(version.id, { onSuccess: () => onOpenChange(false) })}
              >
                Activate
              </Button>
            </Dialog.Footer>
            <Dialog.CloseTrigger asChild>
              <CloseButton size="sm" />
            </Dialog.CloseTrigger>
          </Dialog.Content>
        </Dialog.Positioner>
      </Portal>
    </Dialog.Root>
  )
}

function EditNoteDialog({ version }: { version: AstaVersion }) {
  const [open, setOpen] = useState(false)
  const [note, setNote] = useState('')
  const update = useUpdateAstaVersionNote()

  function onOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen) setNote(version.note ?? '')
    update.reset()
  }

  function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    update.mutate({ id: version.id, note }, { onSuccess: () => onOpenChange(false) })
  }

  return (
    <Dialog.Root open={open} onOpenChange={(e) => onOpenChange(e.open)}>
      <Dialog.Trigger asChild>
        <Button size="xs" variant="outline">Note</Button>
      </Dialog.Trigger>
      <Portal>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.Header>
              <Dialog.Title>Edit note</Dialog.Title>
            </Dialog.Header>
            <form onSubmit={onSubmit}>
              <Dialog.Body>
                <Stack gap={4}>
                  {update.isError && (
                    <Alert.Root status="error">
                      <Alert.Indicator />
                      <Alert.Content>
                        <Alert.Description>{errorMessage(update.error)}</Alert.Description>
                      </Alert.Content>
                    </Alert.Root>
                  )}
                  <Field.Root>
                    <Field.Label>Note</Field.Label>
                    <Input
                      value={note}
                      maxLength={200}
                      placeholder="e.g. version presented at SBES 2026"
                      onChange={(e) => setNote(e.target.value)}
                    />
                    <Field.HelperText>At most 200 characters. Leave empty to clear it.</Field.HelperText>
                  </Field.Root>
                </Stack>
              </Dialog.Body>
              <Dialog.Footer>
                <Dialog.ActionTrigger asChild>
                  <Button variant="outline" type="button">Cancel</Button>
                </Dialog.ActionTrigger>
                <Button type="submit" colorPalette="green" loading={update.isPending}>Save</Button>
              </Dialog.Footer>
            </form>
            <Dialog.CloseTrigger asChild>
              <CloseButton size="sm" />
            </Dialog.CloseTrigger>
          </Dialog.Content>
        </Dialog.Positioner>
      </Portal>
    </Dialog.Root>
  )
}

function DeleteVersionDialog({ version }: { version: AstaVersion }) {
  const [open, setOpen] = useState(false)
  const remove = useDeleteAstaVersion()

  function onOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    remove.reset()
  }

  return (
    <Dialog.Root open={open} onOpenChange={(e) => onOpenChange(e.open)} role="alertdialog">
      <Dialog.Trigger asChild>
        <Button size="xs" variant="outline" colorPalette="red">Delete</Button>
      </Dialog.Trigger>
      <Portal>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.Header>
              <Dialog.Title>Delete version</Dialog.Title>
            </Dialog.Header>
            <Dialog.Body>
              <Stack gap={4}>
                {remove.isError && (
                  <Alert.Root status="error">
                    <Alert.Indicator />
                    <Alert.Content>
                      <Alert.Description>{errorMessage(remove.error)}</Alert.Description>
                    </Alert.Content>
                  </Alert.Root>
                )}
                <Text>
                  This removes the archived .asta file for good — it can no longer be activated or
                  downloaded. Continue?
                </Text>
                <Box borderWidth="1px" borderRadius="md" p={3} bg="bg.subtle">
                  <Text fontWeight="bold">{version.originalFilename ?? 'astah_seon.asta'}</Text>
                  <Text fontSize="sm" color="fg.muted">
                    {formatDate(version.uploadedAt)} · {formatSize(version.sizeBytes)}
                  </Text>
                </Box>
              </Stack>
            </Dialog.Body>
            <Dialog.Footer>
              <Dialog.ActionTrigger asChild>
                <Button variant="outline">Cancel</Button>
              </Dialog.ActionTrigger>
              <Button
                colorPalette="red"
                loading={remove.isPending}
                onClick={() => remove.mutate(version.id, { onSuccess: () => onOpenChange(false) })}
              >
                Delete
              </Button>
            </Dialog.Footer>
            <Dialog.CloseTrigger asChild>
              <CloseButton size="sm" />
            </Dialog.CloseTrigger>
          </Dialog.Content>
        </Dialog.Positioner>
      </Portal>
    </Dialog.Root>
  )
}

function formatDate(iso: string): string {
  const date = new Date(iso)
  return Number.isNaN(date.getTime()) ? iso : date.toLocaleString()
}

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function errorMessage(err: unknown): string {
  const response = (err as { response?: { status?: number; data?: { error?: string } } })
    ?.response
  if (response?.status === 401 || response?.status === 403) {
    return 'Your session expired. Please log in again.'
  }
  return response?.data?.error ?? 'Something went wrong. Please try again.'
}
