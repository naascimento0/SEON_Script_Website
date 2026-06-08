import {
  Box,
  Code,
  Em,
  Flex,
  Heading,
  Link,
  Separator,
  Table,
  Text,
  VStack,
} from '@chakra-ui/react'
import { useParams } from 'react-router-dom'
import { useOntology } from '../api/queries'
import DiagramWithMap from '../components/DiagramWithMap'
import HtmlContent from '../components/HtmlContent'
import Section from '../components/Section'
import type {
  ConceptDetail,
  ConceptRow,
  DependencyView,
  OntologyPageResponse,
} from '../types/api'

export default function OntologyPage() {
  const { name } = useParams<{ name: string }>()
  const { data, isLoading, isError } = useOntology(name)

  if (isLoading) return <Text>Loading…</Text>
  if (isError || !data) {
    return (
      <VStack align="start" gap={4}>
        <Heading>Ontology not found</Heading>
        <Text>The ontology "{name}" was not found.</Text>
      </VStack>
    )
  }

  return (
    <VStack align="stretch" gap={10}>
      <PageHeader data={data} />
      <DescriptionSection data={data} />
      <RelatedOntologiesSection data={data} />
      <OntologyModelsSection data={data} />
      <ConceptsDefinitionSection data={data} />
      <DetailedConceptsSection data={data} />
    </VStack>
  )
}

function PageHeader({ data }: { data: OntologyPageResponse }) {
  return (
    <Box bg="bg.subtle" p={8} borderRadius="md">
      <Heading size="3xl">The {data.title}</Heading>
      {data.ontoLevelText && (
        <Text fontStyle="italic" textAlign="end" mt={3} color="fg.muted">
          {data.ontoLevelText}
        </Text>
      )}
    </Box>
  )
}

function DescriptionSection({ data }: { data: OntologyPageResponse }) {
  return (
    <Box id="ontologydescription">
      <Heading size="2xl" mb={4}>
        1. Ontology Description
      </Heading>
      {data.description ? (
        <HtmlContent fontSize="lg" html={data.description} />
      ) : (
        <Text color="red.500" fontWeight="bold" fontSize="lg">
          No definition available
        </Text>
      )}
    </Box>
  )
}

function RelatedOntologiesSection({ data }: { data: OntologyPageResponse }) {
  return (
    <Box id="relatedontologies">
      <Heading size="2xl" mb={4}>
        2. Related Ontologies
      </Heading>
      <Text fontSize="lg" mb={4}>
        Networked ontologies used by <strong>{data.shortName}</strong>:
      </Text>
      <Box overflowX="auto">
        <Table.Root size="md" interactive>
          <Table.Header>
            <Table.Row>
              <Table.ColumnHeader>Ontology</Table.ColumnHeader>
              <Table.ColumnHeader>Relation</Table.ColumnHeader>
              <Table.ColumnHeader>Integration Level</Table.ColumnHeader>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {data.dependencies.length === 0 ? (
              <Table.Row>
                <Table.Cell colSpan={3} textAlign="center">
                  No dependencies available
                </Table.Cell>
              </Table.Row>
            ) : (
              data.dependencies.map((d) => (
                <DependencyRow key={d.shortName} dep={d} />
              ))
            )}
          </Table.Body>
        </Table.Root>
      </Box>
    </Box>
  )
}

function DependencyRow({ dep }: { dep: DependencyView }) {
  return (
    <Table.Row>
      <Table.Cell>
        <Link href={dep.url} target={dep.openInNewTab ? '_blank' : undefined}>
          {dep.shortName} — {dep.fullName}
        </Link>
      </Table.Cell>
      <Table.Cell>
        {dep.description ? <HtmlContent html={dep.description} /> : 'No description'}
      </Table.Cell>
      <Table.Cell textAlign="center">{dep.level}</Table.Cell>
    </Table.Row>
  )
}

function OntologyModelsSection({ data }: { data: OntologyPageResponse }) {
  const hasContent = data.diagrams.length > 0 || data.sections.length > 0
  return (
    <Box id="ontologymodels">
      <Heading size="2xl" mb={4}>
        3. Ontology Models
      </Heading>

      {data.diagrams.map((d) => (
        <DiagramWithMap key={d.figureNumber} diagram={d} />
      ))}

      {!hasContent && (
        <Text textAlign="center" fontSize="lg">
          No diagrams available for this package.
        </Text>
      )}

      {data.sections.map((s) => (
        <Section key={s.sectionRef} section={s} />
      ))}
    </Box>
  )
}

function ConceptsDefinitionSection({ data }: { data: OntologyPageResponse }) {
  return (
    <Box id="conceptsdefinition">
      <Heading size="2xl" mb={4}>
        4. Concepts Definition
      </Heading>
      <Text fontSize="lg" mb={4}>
        The following table shows the definitions for{' '}
        <strong>{data.shortName}</strong> concepts.
      </Text>
      <Box overflowX="auto">
        <Table.Root size="md" interactive>
          <Table.Header>
            <Table.Row>
              <Table.ColumnHeader width="30%">Concept</Table.ColumnHeader>
              <Table.ColumnHeader>Definition</Table.ColumnHeader>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {data.conceptRows.length === 0 ? (
              <Table.Row>
                <Table.Cell colSpan={2} textAlign="center">
                  No concepts available
                </Table.Cell>
              </Table.Row>
            ) : (
              data.conceptRows.map((c) => <ConceptRowItem key={c.label} row={c} />)
            )}
          </Table.Body>
        </Table.Root>
      </Box>
    </Box>
  )
}

function ConceptRowItem({ row }: { row: ConceptRow }) {
  const style = styleClassToProps(row.styleClass)
  return (
    <Table.Row>
      <Table.Cell>
        <Box id={row.label}>
          <Text as="span" {...style}>
            {row.name}
          </Text>
          <Link ml={2} href={`#${row.detailLabel}`} color="fg.muted">
            (+)
          </Link>
        </Box>
      </Table.Cell>
      <Table.Cell>
        <Text>{row.definition}</Text>
        {row.example && (
          <Text mt={1}>
            <Text as="span" fontWeight="light">E.g.: </Text>
            <Em>{row.example}</Em>
          </Text>
        )}
        {row.source && (
          <Text mt={1}>
            <Text as="span" fontWeight="light">Src.: </Text>
            {row.source}
          </Text>
        )}
      </Table.Cell>
    </Table.Row>
  )
}

function styleClassToProps(styleClass: string): Record<string, unknown> {
  const props: Record<string, unknown> = {}
  if (styleClass.includes('fst-italic')) props.fontStyle = 'italic'
  if (styleClass.includes('fw-bold')) props.fontWeight = 'bold'
  return props
}

function DetailedConceptsSection({ data }: { data: OntologyPageResponse }) {
  return (
    <Box id="detailedconcepts">
      <Heading size="2xl" mb={4}>
        5. Detailed Concepts
      </Heading>
      <Text fontSize="lg" mb={4}>
        {data.title} detailed concepts.
      </Text>
      {data.conceptDetails.length === 0 ? (
        <Text textAlign="center">No detailed concepts available</Text>
      ) : (
        data.conceptDetails.map((d) => (
          <ConceptDetailCard key={d.detailLabel} detail={d} />
        ))
      )}
    </Box>
  )
}

function ConceptDetailCard({ detail }: { detail: ConceptDetail }) {
  return (
    <>
      <Separator my={6} />
      <Box id={detail.detailLabel}>
        <Heading as="h4" size="md" mb={4}>
          {detail.fullName}
        </Heading>
        <Flex gap={6} direction={{ base: 'column', md: 'row' }}>
          <Box flex="1">
            <Box borderWidth="1px" borderColor="border" p={3} mb={3} textAlign="center">
              {detail.stereotype && (
                <Code colorPalette="gray">&laquo;{detail.stereotype}&raquo;</Code>
              )}
              <Text fontWeight="bold" mt={1}>
                {detail.name}
              </Text>
            </Box>
            <Heading as="h5" size="sm" mb={2}>
              Specializes:
            </Heading>
            {detail.generalizations.length === 0 ? (
              <Text>No specializations</Text>
            ) : (
              detail.generalizations.map((gen) => <Text key={gen}>{gen}</Text>)
            )}
          </Box>

          <Box flex="1">
            <Heading as="h5" size="sm" mb={2}>
              Definition:
            </Heading>
            <Text>{detail.definition}</Text>
            {detail.example && (
              <Text mt={2}>
                <Text as="span" fontWeight="bold">Example: </Text>
                <Em>{detail.example}</Em>
              </Text>
            )}
            {detail.source && (
              <Text mt={2}>
                <Text as="span" fontWeight="bold">Source: </Text>
                {detail.source}
              </Text>
            )}
          </Box>

          <Box flex="1">
            <Heading as="h5" size="sm" mb={2}>
              Relations:
            </Heading>
            {detail.relations.length === 0 ? (
              <Text color="fg.muted">No relations</Text>
            ) : (
              <Code
                display="block"
                whiteSpace="pre-wrap"
                colorPalette="gray"
                p={2}
              >
                {detail.relations.map((rel, i) => (
                  <Box key={i}>
                    <HtmlContent as="span" html={rel} />
                  </Box>
                ))}
              </Code>
            )}
          </Box>
        </Flex>
      </Box>
    </>
  )
}
