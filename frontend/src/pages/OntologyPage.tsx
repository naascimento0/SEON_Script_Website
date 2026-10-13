import {
  Badge,
  Box,
  Button,
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
import { useEffect } from 'react'
import { Link as RouterLink, useLocation, useParams } from 'react-router-dom'
import { useCurrentUser, useOntology, useOntoumlCoverage } from '../api/queries'
import { isAppRoute, withBase } from '../lib/routes'
import DiagramWithMap from '../components/DiagramWithMap'
import HtmlContent from '../components/HtmlContent'
import Section from '../components/Section'
import type {
  ConceptDetail,
  ConceptRow,
  DependencyView,
  OntologyPageResponse,
  OntoumlCoverage,
} from '../types/api'

export default function OntologyPage() {
  const { name } = useParams<{ name: string }>()
  const { hash } = useLocation()
  const { data, isLoading, isError } = useOntology(name)
  const { data: user } = useCurrentUser()
  const isAdmin = user?.roles.includes('ROLE_ADMIN') ?? false

  // A hotspot links straight to a concept on another ontology's page. The browser cannot honour
  // that fragment on its own: the anchor only exists once this query has resolved and rendered.
  useEffect(() => {
    if (!data || !hash) return
    const target = document.getElementById(decodeURIComponent(hash.slice(1)))
    if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [data, hash])

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
      {isAdmin && <OntoumlCoverageSection name={data.shortName} />}
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
      <Flex mt={6} gap={4} align="center" wrap="wrap">
        <Button asChild colorPalette="gray" size="sm">
          <a
            href={withBase(`/api/ontouml/${encodeURIComponent(data.shortName)}`)}
            download
          >
            Download OntoUML (JSON)
          </a>
        </Button>
        <Text fontSize="sm" color="fg.muted" flex="1" minW="240px">
          Importable into Visual Paradigm with the OntoUML plugin. The file carries the model
          only (no diagrams), and relations to other ontologies are left out.
        </Text>
      </Flex>
    </Box>
  )
}

/** Admin-only: which classes would reach Visual Paradigm without an OntoUML stereotype. */
function OntoumlCoverageSection({ name }: { name: string }) {
  const { data, isLoading, isError } = useOntoumlCoverage(name, true)

  if (isLoading) return <Text color="fg.muted">Loading OntoUML coverage…</Text>
  if (isError || !data) {
    return <Text color="red.500">Could not load the OntoUML stereotype coverage.</Text>
  }
  return <CoverageReport report={data} />
}

function CoverageReport({ report }: { report: OntoumlCoverage }) {
  const { totalClasses, mappedToOntoUml, missingStereotype, unknownStereotype } = report
  const percent = totalClasses === 0 ? 100 : Math.round((mappedToOntoUml / totalClasses) * 100)
  const complete = missingStereotype.length === 0 && unknownStereotype.length === 0
  const rawCounts = Object.entries(report.rawStereotypeCounts).sort((a, b) => b[1] - a[1])

  return (
    <Box borderWidth="1px" borderColor="border.emphasized" borderRadius="md" p={5}>
      <Flex gap={3} align="center" wrap="wrap" mb={2}>
        <Heading size="md">OntoUML stereotype coverage</Heading>
        <Badge colorPalette="purple">admin</Badge>
        <Badge colorPalette={complete ? 'green' : 'orange'}>
          {mappedToOntoUml}/{totalClasses} classes ({percent}%)
        </Badge>
      </Flex>
      <Text fontSize="sm" color="fg.muted" mb={complete ? 0 : 3}>
        Classes without a recognised stereotype are imported into Visual Paradigm as plain UML
        classes. Fix them in the Astah model and upload it again.
      </Text>

      {!complete && (
        <details>
          <summary style={{ cursor: 'pointer' }}>Show the classes to fix</summary>
          <VStack align="stretch" gap={4} mt={3}>
            <ClassList
              title="Without stereotype"
              items={missingStereotype}
            />
            <ClassList
              title="Stereotype not recognised as OntoUML"
              items={unknownStereotype}
            />
            {rawCounts.length > 0 && (
              <Box>
                <Text fontWeight="bold" mb={1}>Stereotypes found in the model</Text>
                <Flex gap={2} wrap="wrap">
                  {rawCounts.map(([stereotype, count]) => (
                    <Badge key={stereotype} variant="outline">
                      «{stereotype}» {count}
                    </Badge>
                  ))}
                </Flex>
              </Box>
            )}
          </VStack>
        </details>
      )}
    </Box>
  )
}

function ClassList({ title, items }: { title: string; items: string[] }) {
  if (items.length === 0) return null
  return (
    <Box>
      <Text fontWeight="bold" mb={1}>
        {title} ({items.length})
      </Text>
      <Text fontSize="sm">{[...items].sort((a, b) => a.localeCompare(b)).join(', ')}</Text>
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
        {isAppRoute(dep.url) ? (
          <Link asChild>
            <RouterLink to={dep.url}>
              {dep.shortName} — {dep.fullName}
            </RouterLink>
          </Link>
        ) : (
          <Link href={dep.url} target={dep.openInNewTab ? '_blank' : undefined}>
            {dep.shortName} — {dep.fullName}
          </Link>
        )}
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
