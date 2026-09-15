import {
  Box,
  Button,
  Container,
  Flex,
  HStack,
  Heading,
  Image,
  Link,
  Spacer,
  Stack,
  Text,
} from '@chakra-ui/react'
import { Link as RouterLink, Outlet } from 'react-router-dom'
import { useCurrentUser, useLogout } from '../api/queries'

/** Served by the backend from `static/images`; base-aware so it also works under /seon2. */
const NEMO_LOGO_SRC = `${import.meta.env.BASE_URL.replace(/\/$/, '')}/images/logo_nemo.svg`

const CONTACT_EMAILS = ['monalessa@inf.ufes.br', 'gabriel.n.oliveira@edu.ufes.br']

const HEADER_BG = '#d2e7d1'

export default function Layout() {
  const { data: user } = useCurrentUser()
  const logout = useLogout()

  return (
    <Flex direction="column" minH="100vh">
      <Box
        as="header"
        bg={HEADER_BG}
        borderBottomWidth="1px"
        borderBottomColor="black"
        px={6}
        py={3}
      >
        <HStack gap={6}>
          <Heading size="md">
            <Link asChild>
              <RouterLink to="/">SEON</RouterLink>
            </Link>
          </Heading>
          <Link asChild>
            <RouterLink to="/ontologies">Ontologies</RouterLink>
          </Link>
          <Link asChild>
            <RouterLink to="/publications">Publications</RouterLink>
          </Link>
          <Link asChild>
            <RouterLink to="/suggestions">Suggestions</RouterLink>
          </Link>
          <Spacer />
          {user ? (
            <HStack gap={3}>
              <Link asChild>
                <RouterLink to="/upload">Upload</RouterLink>
              </Link>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => logout.mutate()}
                disabled={logout.isPending}
              >
                Logout ({user.username})
              </Button>
            </HStack>
          ) : (
            <Link asChild>
              <RouterLink to="/login">Login</RouterLink>
            </Link>
          )}
        </HStack>
      </Box>

      <Container as="main" maxW="6xl" py={6} flex="1">
        <Outlet />
      </Container>

      <Box as="footer" borderTopWidth="1px" px={6} py={6}>
        <Flex
          maxW="6xl"
          mx="auto"
          gap={6}
          align="center"
          justify="space-between"
          direction={{ base: 'column', md: 'row' }}
          textAlign={{ base: 'center', md: 'left' }}
        >
          <Stack gap={1} fontSize="sm" align={{ base: 'center', md: 'flex-start' }}>
            <Text fontWeight="medium">SEON — Software Engineering Ontology Network</Text>
            <HStack gap={2} wrap="wrap" justify={{ base: 'center', md: 'flex-start' }}>
              {CONTACT_EMAILS.map((email, i) => (
                <HStack gap={2} key={email}>
                  {i > 0 && <Text color="fg.muted" aria-hidden="true">·</Text>}
                  <Link href={`mailto:${email}`}>{email}</Link>
                </HStack>
              ))}
            </HStack>
          </Stack>

          <Link
            href="https://nemo.inf.ufes.br"
            target="_blank"
            rel="noopener noreferrer"
            fontSize="sm"
            color="fg.muted"
            _hover={{ textDecoration: 'none', color: 'fg' }}
          >
            <HStack gap={3}>
              <Text>Powered by NEMO</Text>
              <Image
                src={NEMO_LOGO_SRC}
                alt="NEMO logo"
                h="40px"
                w="auto"
              />
            </HStack>
          </Link>
        </Flex>
      </Box>
    </Flex>
  )
}
