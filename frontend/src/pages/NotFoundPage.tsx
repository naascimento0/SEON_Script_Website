import { Heading, Text, VStack } from '@chakra-ui/react'

export default function NotFoundPage() {
  return (
    <VStack align="start" gap={4}>
      <Heading size="xl">404</Heading>
      <Text color="fg.muted">Page not found.</Text>
    </VStack>
  )
}
