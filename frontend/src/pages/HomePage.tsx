import {
  Box,
  Button,
  Heading,
  Image,
  Link,
  List,
  Text,
  VStack,
} from '@chakra-ui/react'

export default function HomePage() {
  return (
    <VStack align="stretch" gap={10}>
      <Box bg="bg.subtle" p={8} borderRadius="md" textAlign="center">
        <Heading size="3xl">SEON: The Software Engineering Ontology Network</Heading>
      </Box>

      <Box id="definition">
        <Heading size="2xl" mb={4}>SEON's Definition</Heading>
        <Text fontSize="lg" mb={3}>
          SEON provides a well-grounded network of SE reference ontologies, and mechanisms
          for deriving and incorporating new integrated domain ontologies into the network.
        </Text>
        <Text fontSize="lg" mb={3}>
          SEON results from various efforts on building ontologies for the Software
          Engineering (SE) field. Although SEON itself is a new proposal, the studies and
          ontologies developed along the years are important contributions for defining
          this network. Hence, SEON rises with three main premises:
        </Text>
        <List.Root as="ol" fontSize="lg" pl={6} mb={3}>
          <List.Item>Being based on a well-founded grounding for ontology development;</List.Item>
          <List.Item>Offering mechanisms to support building and integrating new SE domain ontologies to the network; and</List.Item>
          <List.Item>Promoting integration by keeping a consistent semantics for concepts and relations along the whole network.</List.Item>
        </List.Root>
        <Text fontSize="lg">
          Currently, the SEON Network is composed of the networked ontologies shown in Figure 1.
        </Text>
        <Box textAlign="center" my={6}>
          <Image
            src="/images/astah_seon/SEON%20Architecture.png"
            alt="SEON Network view"
            maxW="100%"
            mx="auto"
          />
          <Text fontWeight="bold" mt={3}>Figure 1. SEON Network View.</Text>
        </Box>
      </Box>

      <Box id="architecture">
        <Heading size="2xl" mb={4}>SEON's Architecture</Heading>
        <Text fontSize="lg" mb={3}>
          SEON architecture is organized considering three ontology generality levels as Figure 2 shows.
        </Text>
        <Box textAlign="center" my={6}>
          <Image
            src="/images/SEON_Architecture.png"
            alt="SEON Architecture"
            maxW="100%"
            mx="auto"
          />
          <Text fontWeight="bold" mt={3}>Figure 2. SEON Architecture.</Text>
        </Box>

        <Heading as="h3" id="foundational" size="lg" mt={6} mb={2}>Foundational Layer</Heading>
        <Text fontSize="lg">
          At the bottom of SEON, there is the Unified Foundational Ontology (UFO), which is
          developed based on a number of theories from Formal Ontology, Philosophical Logics,
          Philosophy of Language, Linguistics, and Cognitive Psychology. UFO [
          <Link
            href="http://doc.utwente.nl/50826/1/thesis_Guizzardi.pdf"
            target="_blank"
            color="fg"
          >1</Link>
          ] is divided in three parts: an ontology of endurants (objects), an ontology of
          perdurants (events), and an ontology of social entities. UFO's ontological
          distinctions are used for classifying SEON concepts, e.g., as <i>objects</i>,{' '}
          <i>actions</i>, <i>commitments</i>, <i>agents</i>, <i>roles</i>, <i>goals</i>,
          and so on. UFO provides the necessary grounding for the concepts and relations of
          all networked ontologies.
        </Text>

        <Heading as="h3" id="core" size="lg" mt={6} mb={2}>Core Layer</Heading>
        <Text fontSize="lg">
          In the center of SEON, providing the SE core knowledge for the network, there is
          the Software Process Ontology (SPO). SPO is a core ontology grounded in UFO,
          aiming at establishing a common conceptualization on software processes. SPO scope
          embraces the following aspects of the software process domain: standard, project,
          and performed processes and their activities, artifacts handled, resources used,
          and procedures adopted, and stakeholders participation. SPO reuses some concepts
          from the Enterprise Ontology (EO), a core ontology on enterprises (external to
          SEON), for dealing with aspects related to organizations, such as team membership.
          Another external ontology related to this layer is the Core Ontology on
          Measurements (COM).
        </Text>

        <Heading as="h3" id="domain" size="lg" mt={6} mb={2}>Domain-specific Layer</Heading>
        <Text fontSize="lg" mb={3}>
          Over the foundational and core layers, domain ontologies appear. Each domain
          networked ontology is grounded in core ontologies and in UFO, and encompasses a SE
          subdomain (e.g., software requirements, design, configuration management, and
          measurement). Although not explicit in Figure 2, more specific domain ontologies
          can be developed based on other more general domain ontologies. For instance,
          ontologies addressing runtime requirements (RRO), goal-oriented requirements
          (GORO), and the requirements development process (RDPO) were developed based on
          the Reference Software Requirements Ontology (RSRO). For matter of organization,
          domain networked ontologies can be grouped in subnetworks, as the Requirements
          engineering Ontology subNetwork (ReqON).
        </Text>
        <Text fontSize="lg">
          In a nutshell, the foundational ontology offers the ontological distinctions for
          the core and domain layers, while the core layer offers the SE core knowledge for
          building the domain networked ontologies. This way of grounding the ontologies in
          the network is helpful for engineering the networked ontologies, since it provides
          ontological consistency and makes a number of modeling decisions easier.
        </Text>
      </Box>

      <Box id="downloads">
        <Heading size="2xl" mb={4}>Downloads</Heading>
        <Text fontSize="lg" mb={4}>
          SEON is also available as an OWL file containing the core and domain layers of the
          network. The file is generated on demand from the current Astah model and can be
          opened in tools like Protégé.
        </Text>
        <Button asChild colorPalette="gray">
          <a href="/seon.owl" download>Download SEON.owl</a>
        </Button>
      </Box>
    </VStack>
  )
}
