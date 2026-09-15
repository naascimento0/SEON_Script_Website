export interface MapArea {
  coords: string
  href: string
  target: string
  alt: string
}

export interface DiagramView {
  figureNumber: number
  introText: string
  labelText: string
  description: string | null
  imageSrc: string
  imageWidth: number
  mapName: string
  mapAreas: MapArea[]
  simpleImage: boolean
}

export interface SectionView {
  sectionNumber: string
  headingLevel: number
  sectionRef: string
  sectionName: string
  description: string | null
  diagrams: DiagramView[]
  subsections: SectionView[]
}

export interface DependencyView {
  shortName: string
  fullName: string
  url: string
  openInNewTab: boolean
  description: string | null
  level: string
}

export interface ConceptRow {
  name: string
  styleClass: string
  label: string
  detailLabel: string
  definition: string
  example: string | null
  source: string | null
}

export interface ConceptDetail {
  fullName: string
  name: string
  stereotype: string | null
  detailLabel: string
  definition: string
  example: string | null
  source: string | null
  generalizations: string[]
  relations: string[]
}

export interface OntologyPageResponse {
  title: string
  shortName: string
  fullName: string
  ontoLevelIcon: string | null
  ontoLevelText: string
  description: string | null
  dependencies: DependencyView[]
  diagrams: DiagramView[]
  sections: SectionView[]
  conceptRows: ConceptRow[]
  conceptDetails: ConceptDetail[]
}

export interface OntologyListItem {
  name: string
  shortName: string
  fullName: string
  level: string | null
  network: string | null
}

export interface AuthUser {
  username: string
  roles: string[]
}

export type PublicationCategory = 'SEON_ONTOLOGY' | 'GENERAL'

export interface Publication {
  id: string
  category: PublicationCategory
  year: number
  ontology: string | null
  reference: string
  link: string | null
}

/** Payload for POST /api/publications (admin only). */
export interface NewPublication {
  category: PublicationCategory
  year: number
  ontology?: string
  reference: string
  link?: string
}

/** One archived `.asta` upload, from the admin-only version history. */
export interface AstaVersion {
  id: string
  originalFilename: string | null
  /** ISO-8601 instant, UTC. */
  uploadedAt: string
  uploadedBy: string
  sizeBytes: number
  sha256: string
  note: string | null
  ontologyCount: number
  conceptCount: number
  /** True for the version the site is currently serving. */
  active: boolean
}
