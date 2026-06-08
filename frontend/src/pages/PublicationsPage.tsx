import { Badge, Box, Heading, Link, Table, Text, VStack } from '@chakra-ui/react'
import HtmlContent from '../components/HtmlContent'
import {
  generalSeonPublications,
  seonOntologyPublications,
  type Publication,
} from '../data/publications'

export default function PublicationsPage() {
  return (
    <VStack align="stretch" gap={10}>
      <Box bg="bg.subtle" p={8} borderRadius="md" textAlign="center">
        <Heading size="3xl">Publications</Heading>
        <Text fontSize="lg" mt={3}>
          Research papers associated with SEON ontologies and the SEON network.
        </Text>
      </Box>

      <Box id="seon-ontologies">
        <Heading size="2xl" mb={4}>1. Publications on SEON Ontologies</Heading>
        <Text fontSize="lg" mb={4}>Papers associated with SEON ontologies.</Text>
        <PublicationsTable
          publications={seonOntologyPublications}
          showOntology
        />
      </Box>

      <Box id="general-seon">
        <Heading size="2xl" mb={4}>2. General SEON Publications</Heading>
        <Text fontSize="lg" mb={4}>
          Papers that address SEON broadly without focusing on a specific ontology, listed
          chronologically.
        </Text>
        <PublicationsTable publications={generalSeonPublications} />
      </Box>
    </VStack>
  )
}

interface TableProps {
  publications: Publication[]
  showOntology?: boolean
}

function PublicationsTable({ publications, showOntology = false }: TableProps) {
  return (
    <Box overflowX="auto">
      <Table.Root size="md" interactive>
        <Table.Header>
          <Table.Row>
            <Table.ColumnHeader width="80px">Year</Table.ColumnHeader>
            {showOntology && <Table.ColumnHeader width="160px">Ontology</Table.ColumnHeader>}
            <Table.ColumnHeader>Reference</Table.ColumnHeader>
            <Table.ColumnHeader width="80px">Link</Table.ColumnHeader>
          </Table.Row>
        </Table.Header>
        <Table.Body>
          {publications.map((p, i) => (
            <Table.Row key={`${p.year}-${i}`}>
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
            </Table.Row>
          ))}
        </Table.Body>
      </Table.Root>
    </Box>
  )
}
