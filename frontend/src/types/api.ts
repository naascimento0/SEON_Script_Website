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
  shortName: string
  fullName: string
  level: string | null
  network: string | null
}

export interface AuthUser {
  username: string
  roles: string[]
}
