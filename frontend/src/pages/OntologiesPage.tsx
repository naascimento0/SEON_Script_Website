import {
  Badge,
  Box,
  Heading,
  HStack,
  Link,
  SimpleGrid,
  Spinner,
  Text,
  VStack,
} from '@chakra-ui/react'
import { Link as RouterLink } from 'react-router-dom'
import { useOntologyList } from '../api/queries'
import type { OntologyListItem } from '../types/api'

// Layers in SEON's conceptual order: foundational at the bottom, then core, then domain.
const LEVEL_ORDER = ['FOUNDATIONAL', 'CORE', 'DOMAIN'] as const

const LEVEL_LABELS: Record<string, string> = {
  FOUNDATIONAL: 'Foundational Layer',
  CORE: 'Core Layer',
  DOMAIN: 'Domain-specific Layer',
}

const LEVEL_DESCRIPTIONS: Record<string, string> = {
  FOUNDATIONAL:
    'The Unified Foundational Ontology (UFO) that grounds every ontology in the network.',
  CORE: 'Core ontologies providing the shared Software Engineering knowledge for the network.',
  DOMAIN:
    'Domain networked ontologies, each covering a specific Software Engineering subdomain.',
}

const OTHER_LABEL = 'Other Ontologies'

export default function OntologiesPage() {
  const { data, isLoading, isError } = useOntologyList()

  return (
    <VStack align="stretch" gap={10}>
      <Box bg="bg.subtle" p={8} borderRadius="md" textAlign="center">
        <Heading size="3xl">Ontologies</Heading>
        <Text fontSize="lg" mt={3}>
          The networked ontologies that make up SEON, organized by generality level.
        </Text>
      </Box>

      {isLoading && (
        <HStack justify="center" py={10}>
          <Spinner />
          <Text>Loading ontologies…</Text>
        </HStack>
      )}

      {isError && (
        <Text color="fg.error" textAlign="center" py={10}>
          Could not load the ontology list. Please try again later.
        </Text>
      )}

      {data && data.length === 0 && (
        <Text textAlign="center" py={10}>
          No ontologies are available yet.
        </Text>
      )}

      {data &&
        data.length > 0 &&
        groupByLevel(data).map(({ level, items }) => (
          <Box key={level} id={level.toLowerCase()}>
            <Heading size="2xl" mb={1}>
              {LEVEL_LABELS[level] ?? OTHER_LABEL}
            </Heading>
            {LEVEL_DESCRIPTIONS[level] && (
              <Text fontSize="md" color="fg.muted" mb={4}>
                {LEVEL_DESCRIPTIONS[level]}
              </Text>
            )}
            <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} gap={4}>
              {items.map((o) => (
                <OntologyCard key={o.name} ontology={o} />
              ))}
            </SimpleGrid>
          </Box>
        ))}
    </VStack>
  )
}

function OntologyCard({ ontology }: { ontology: OntologyListItem }) {
  return (
    <Link
      asChild
      _hover={{ textDecoration: 'none' }}
      borderWidth="1px"
      borderRadius="md"
      p={4}
      transition="border-color 0.15s, box-shadow 0.15s"
      _focusVisible={{ outlineColor: 'fg' }}
      css={{ '&:hover': { borderColor: 'fg', boxShadow: 'sm' } }}
    >
      <RouterLink to={`/ontology/${encodeURIComponent(ontology.name)}`}>
        <VStack align="stretch" gap={2}>
          <HStack justify="space-between" align="start">
            <Heading size="md">{ontology.shortName || ontology.name}</Heading>
            {ontology.network && (
              <Badge colorPalette="gray" flexShrink={0}>
                {ontology.network}
              </Badge>
            )}
          </HStack>
          {ontology.fullName && (
            <Text fontSize="sm" color="fg.muted">
              {ontology.fullName}
            </Text>
          )}
        </VStack>
      </RouterLink>
    </Link>
  )
}

interface LevelGroup {
  level: string
  items: OntologyListItem[]
}

// Groups ontologies by level, preserving SEON's foundational→core→domain order and
// appending any ungrouped/unknown levels at the end. Items keep the API's alphabetical order.
function groupByLevel(items: OntologyListItem[]): LevelGroup[] {
  const buckets = new Map<string, OntologyListItem[]>()
  for (const item of items) {
    const key = item.level ?? 'OTHER'
    const bucket = buckets.get(key)
    if (bucket) {
      bucket.push(item)
    } else {
      buckets.set(key, [item])
    }
  }

  const groups: LevelGroup[] = []
  for (const level of LEVEL_ORDER) {
    const bucket = buckets.get(level)
    if (bucket) {
      groups.push({ level, items: bucket })
      buckets.delete(level)
    }
  }
  // Any remaining levels (including unknown ones and "OTHER") go last.
  for (const [level, bucket] of buckets) {
    groups.push({ level, items: bucket })
  }
  return groups
}
