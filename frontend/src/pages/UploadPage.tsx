import {
  Alert,
  Box,
  Button,
  Heading,
  Input,
  Text,
  VStack,
} from '@chakra-ui/react'
import { useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { Navigate } from 'react-router-dom'
import { useCurrentUser, useUploadAsta } from '../api/queries'

export default function UploadPage() {
  const { data: user, isLoading } = useCurrentUser()
  const upload = useUploadAsta()
  const inputRef = useRef<HTMLInputElement | null>(null)
  const [file, setFile] = useState<File | null>(null)
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
    upload.mutate(file)
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
            <Alert.Description>{String(upload.error)}</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}
    </VStack>
  )
}
