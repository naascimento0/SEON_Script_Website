import {
  Alert,
  Box,
  Button,
  Field,
  Heading,
  Input,
  Stack,
  Text,
} from '@chakra-ui/react'
import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useLogin } from '../api/queries'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const navigate = useNavigate()
  const login = useLogin()

  function onSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    login.mutate(
      { username, password },
      { onSuccess: () => navigate('/upload') },
    )
  }

  return (
    <Box maxW="md" mx="auto" borderWidth="1px" borderRadius="md" p={8}>
      <Heading size="xl" textAlign="center" mb={2}>Login Required</Heading>
      <Text textAlign="center" color="fg.muted" mb={6}>
        Please authenticate to access SEON Upload feature
      </Text>

      {login.isError && (
        <Alert.Root status="error" mb={4}>
          <Alert.Indicator />
          <Alert.Content>
            <Alert.Title>Authentication Failed</Alert.Title>
            <Alert.Description>Invalid username or password.</Alert.Description>
          </Alert.Content>
        </Alert.Root>
      )}

      <form onSubmit={onSubmit}>
        <Stack gap={4}>
          <Field.Root required>
            <Field.Label>Username</Field.Label>
            <Input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              autoComplete="username"
            />
          </Field.Root>

          <Field.Root required>
            <Field.Label>Password</Field.Label>
            <Input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
            />
          </Field.Root>

          <Button
            type="submit"
            colorPalette="green"
            loading={login.isPending}
            loadingText="Signing in…"
          >
            Sign In
          </Button>
        </Stack>
      </form>

      <Text textAlign="center" fontSize="sm" color="fg.muted" mt={6}>
        Use your SEON credentials to access the upload functionality.
      </Text>
    </Box>
  )
}
