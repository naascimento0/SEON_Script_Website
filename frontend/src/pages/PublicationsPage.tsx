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
  Link,
  Portal,
  Spinner,
  Stack,
  Table,
  Text,
  Textarea,
  VStack,
} from '@chakra-ui/react'
import { useState, type FormEvent, type ReactNode } from 'react'
import HtmlContent from '../components/HtmlContent'
import {
  useAddPublication,
  useCurrentUser,
  useDeletePublication,
  usePublications,
  useUpdatePublication,
} from '../api/queries'
import type { Publication, PublicationCategory } from '../types/api'

export default function PublicationsPage() {
  const { data: publications, isLoading, isError } = usePublications()
  const { data: user } = useCurrentUser()
  const isAdmin = user?.roles.includes('ROLE_ADMIN') ?? false

  const byCategory = (category: PublicationCategory) =>
    publications?.filter((p) => p.category === category) ?? []

  return (
    <VStack align="stretch" gap={10}>
      <Box bg="bg.subtle" p={8} borderRadius="md" textAlign="center">
        <Heading size="3xl">Publications</Heading>
        <Text fontSize="lg" mt={3}>
          Research papers associated with SEON ontologies and the SEON network.
        </Text>
      </Box>

      {isLoading && (
        <HStack justify="center" py={10}>
          <Spinner />
          <Text>Loading publications…</Text>
        </HStack>
      )}

      {isError && (
        <Alert.Root status="error">
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>Could not load publications</Alert.Title>
            <Alert.Description>Please try again later.</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      {!isLoading && !isError && (
        <>
          <Box id="seon-ontologies">
            <HStack justify="space-between" align="start" mb={4} flexWrap="wrap" gap={3}>
              <Heading size="2xl">1. Publications on SEON Ontologies</Heading>
              {isAdmin && (
                <PublicationFormDialog
                  category="SEON_ONTOLOGY"
                  showOntology
                  trigger={
                    <Button size="sm" colorPalette="green">Add publication</Button>
                  }
                />
              )}
            </HStack>
            <Text fontSize="lg" mb={4}>Papers associated with SEON ontologies.</Text>
            <PublicationsTable
              publications={byCategory('SEON_ONTOLOGY')}
              showOntology
              isAdmin={isAdmin}
            />
          </Box>

          <Box id="general-seon">
            <HStack justify="space-between" align="start" mb={4} flexWrap="wrap" gap={3}>
              <Heading size="2xl">2. General SEON Publications</Heading>
              {isAdmin && (
                <PublicationFormDialog
                  category="GENERAL"
                  trigger={
                    <Button size="sm" colorPalette="green">Add publication</Button>
                  }
                />
              )}
            </HStack>
            <Text fontSize="lg" mb={4}>
              Papers that address SEON broadly without focusing on a specific ontology, listed
              chronologically.
            </Text>
            <PublicationsTable publications={byCategory('GENERAL')} isAdmin={isAdmin} />
          </Box>
        </>
      )}
    </VStack>
  )
}

interface TableProps {
  publications: Publication[]
  showOntology?: boolean
  isAdmin?: boolean
}

function PublicationsTable({
  publications,
  showOntology = false,
  isAdmin = false,
}: TableProps) {
  return (
    <Box overflowX="auto">
      <Table.Root size="md" interactive>
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader width="80px">Year</Table.ColumnHeader>
            {showOntology && <Table.ColumnHeader width="160px">Ontology</Table.ColumnHeader>}
            <Table.ColumnHeader>Reference</Table.ColumnHeader>
            <Table.ColumnHeader width="80px">Link</Table.ColumnHeader>
            {isAdmin && <Table.ColumnHeader width="150px">Actions</Table.ColumnHeader>}
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {publications.map((p) => (
            <Table.Row key={p.id}>
              <Table.Cell>
                <Badge>{p.year}</Badge>
              </Table.Cell>
              {showOntology && (
                <Table.Cell fontWeight="bold">{p.ontology}</Table.Cell>
              )}
              <Table.Cell>
                <HtmlContent html={p.reference} />
              </Table.Cell>
              <Table.Cell>
                {p.link ? (
                  <Link href={p.link} target="_blank">Open</Link>
                ) : (
                  '—'
                )}
              </Table.Cell>
              {isAdmin && (
                <Table.Cell>
                  <HStack gap={2}>
                    <PublicationFormDialog
                      category={p.category}
                      showOntology={showOntology}
                      publication={p}
                      trigger={
                        <Button size="xs" variant="outline">Edit</Button>
                      }
                    />
                    <DeletePublicationDialog publication={p} />
                  </HStack>
                </Table.Cell>
              )}
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
    </Box>
  )
}

interface FormDialogProps {
  category: PublicationCategory
  showOntology?: boolean
  /** When given, the dialog edits this publication instead of creating a new one. */
  publication?: Publication
  trigger: ReactNode
}

/** Admin-only form used both to append a row and to edit an existing one. */
function PublicationFormDialog({
  category,
  showOntology = false,
  publication,
  trigger,
}: FormDialogProps) {
  const isEdit = publication !== undefined
  const [open, setOpen] = useState(false)
  const [year, setYear] = useState('')
  const [ontology, setOntology] = useState('')
  const [reference, setReference] = useState('')
  const [link, setLink] = useState('')
  const add = useAddPublication()
  const update = useUpdatePublication()
  const mutation = isEdit ? update : add

  function onOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen) {
      // The server stores the reference escaped; edit its source text, not the markup.
      setYear(String(publication?.year ?? new Date().getFullYear()))
      setOntology(toSourceText(publication?.ontology ?? ''))
      setReference(toSourceText(publication?.reference ?? ''))
      setLink(publication?.link ?? '')
    }
    mutation.reset()
  }

  function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const payload = {
      category,
      year: Number(year),
      ontology: ontology.trim() || undefined,
      reference: reference.trim(),
      link: link.trim() || undefined,
    }
    const onSuccess = () => onOpenChange(false)
    if (publication) {
      update.mutate({ id: publication.id, publication: payload }, { onSuccess })
    } else {
      add.mutate(payload, { onSuccess })
    }
  }

  return (
    <Dialog.Root open={open} onOpenChange={(e) => onOpenChange(e.open)}>
      <Dialog.Trigger asChild>{trigger}</Dialog.Trigger>
      <Portal>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.Header>
              <Dialog.Title>{isEdit ? 'Edit publication' : 'Add publication'}</Dialog.Title>
            </Dialog.Header>

            <form onSubmit={onSubmit}>
              <Dialog.Body>
                <Stack gap={4}>
                  {mutation.isError && (
                    <Alert.Root status="error">
                      <Alert.Indicator />
                      <Alert.Content>
                        <Alert.Description>{errorMessage(mutation.error)}</Alert.Description>
                      </Alert.Content>
                    </Alert.Root>
                  )}

                  <Field.Root required>
                    <Field.Label>Year</Field.Label>
                    <Input
                      type="number"
                      value={year}
                      min={1900}
                      max={new Date().getFullYear() + 2}
                      onChange={(e) => setYear(e.target.value)}
                    />
                  </Field.Root>

                  {showOntology && (
                    <Field.Root>
                      <Field.Label>Ontology</Field.Label>
                      <Input
                        value={ontology}
                        onChange={(e) => setOntology(e.target.value)}
                        placeholder="e.g. ROoST, or SwO, RSRO, RRO"
                      />
                    </Field.Root>
                  )}

                  <Field.Root required>
                    <Field.Label>Reference</Field.Label>
                    <Textarea
                      rows={5}
                      value={reference}
                      onChange={(e) => setReference(e.target.value)}
                      placeholder="Author, A. (2025). Paper title. In <em>Venue</em>."
                    />
                    <Field.HelperText>
                      Only &lt;em&gt;, &lt;i&gt;, &lt;b&gt;, &lt;strong&gt;, &lt;sup&gt; and
                      &lt;sub&gt; are kept; any other markup is shown as plain text.
                    </Field.HelperText>
                  </Field.Root>

                  <Field.Root>
                    <Field.Label>Link</Field.Label>
                    <Input
                      type="url"
                      value={link}
                      onChange={(e) => setLink(e.target.value)}
                      placeholder="https://nemo.inf.ufes.br/papers/my-paper.pdf"
                    />
                    <Field.HelperText>
                      Full URL to the paper, starting with http:// or https://.
                    </Field.HelperText>
                  </Field.Root>
                </Stack>
              </Dialog.Body>

              <Dialog.Footer>
                <Dialog.ActionTrigger asChild>
                  <Button variant="outline" type="button">Cancel</Button>
                </Dialog.ActionTrigger>
                <Button type="submit" colorPalette="green" loading={mutation.isPending}>
                  {isEdit ? 'Save' : 'Add'}
                </Button>
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

/** Admin-only confirmation before a row is removed for good. */
function DeletePublicationDialog({ publication }: { publication: Publication }) {
  const [open, setOpen] = useState(false)
  const remove = useDeletePublication()

  function onOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    remove.reset()
  }

  function onConfirm() {
    remove.mutate(publication.id, { onSuccess: () => onOpenChange(false) })
  }

  return (
    <Dialog.Root
      open={open}
      onOpenChange={(e) => onOpenChange(e.open)}
      role="alertdialog"
    >
      <Dialog.Trigger asChild>
        <Button size="xs" variant="outline" colorPalette="red">Delete</Button>
      </Dialog.Trigger>
      <Portal>
        <Dialog.Backdrop />
        <Dialog.Positioner>
          <Dialog.Content>
            <Dialog.Header>
              <Dialog.Title>Delete publication</Dialog.Title>
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
                <Text>This removes the publication for good. Continue?</Text>
                <Box borderWidth="1px" borderRadius="md" p={3} bg="bg.subtle">
                  <HtmlContent html={publication.reference} />
                </Box>
              </Stack>
            </Dialog.Body>
            <Dialog.Footer>
              <Dialog.ActionTrigger asChild>
                <Button variant="outline">Cancel</Button>
              </Dialog.ActionTrigger>
              <Button colorPalette="red" loading={remove.isPending} onClick={onConfirm}>
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

/**
 * Turns a stored (escaped) field back into the text an admin typed, so that editing and
 * saving a row does not escape it a second time.
 */
function toSourceText(value: string): string {
  return value
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&amp;/g, '&')
}

function errorMessage(err: unknown): string {
  const response = (err as { response?: { status?: number; data?: { error?: string } } })
    ?.response
  if (response?.status === 401 || response?.status === 403) {
    return 'Your session expired. Please log in again.'
  }
  return response?.data?.error ?? 'Could not save your change. Please try again.'
}
