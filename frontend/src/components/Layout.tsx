import { Box, Button, Container, Flex, HStack, Heading, Link, Spacer } from '@chakra-ui/react'
import { Link as RouterLink, Outlet } from 'react-router-dom'
import { useCurrentUser, useLogout } from '../api/queries'

export default function Layout() {
  const { data: user } = useCurrentUser()
  const logout = useLogout()

  return (
    <Flex direction="column" minH="100vh">
      <Box as="header" borderBottomWidth="1px" px={6} py={3}>
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

      <Box as="footer" borderTopWidth="1px" px={6} py={3} textAlign="center" fontSize="sm">
        SEON — Software Engineering Ontology Network
      </Box>
    </Flex>
  )
}
