// Validates an OntoUML JSON export against what the Visual Paradigm OntoUML plugin
// (ontouml-vp-plugin 0.5.x) actually accepts on import.
//
// Usage: node validate-ontouml.mjs <path-to-ontouml.json> [--schema]
//   e.g. node validate-ontouml.mjs /tmp/sysswo.ontouml.json
//
// Exits 0 when valid, 1 when invalid, 2 on usage/IO error.
//
// WHY NOT JUST THE JSON SCHEMA: there are two incompatible OntoUML JSON formats.
//   * the w3id.org "OntoUML Schema v1.0.2" (bundled at scripts/ontouml-schema.json) is a
//     FLAT graph: `elements` + `root`, with links written as bare id strings;
//   * the VP plugin reads a NESTED tree: `model` + `diagrams`, with links written as
//     {"id", "type"} reference objects.
// The plugin's ProjectDeserializer never looks at `elements`/`root`, so a schema-valid
// v1.0.2 file imports as an empty project. On top of that, the schema cannot express the
// two rules that actually break the import: referential integrity (a dangling id makes
// ReferenceResolver throw "Referenced element in property type does not exist!", failing
// the whole import) and the stereotype vocabulary (the schema accepts any non-empty
// string). So this script checks the plugin's contract directly. `--schema` additionally
// runs the bundled v1.0.2 schema, for files meant for the OntoUML/UFO catalog.

import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const here = dirname(fileURLToPath(import.meta.url))

const args = process.argv.slice(2)
const withSchema = args.includes('--schema')
const instancePath = args.find((a) => !a.startsWith('--'))

if (!instancePath) {
  console.error('usage: node validate-ontouml.mjs <path-to-ontouml.json> [--schema]')
  process.exit(2)
}

let instance
try {
  instance = JSON.parse(readFileSync(resolve(instancePath), 'utf8'))
} catch (e) {
  console.error(`cannot read ${instancePath}: ${e.message}`)
  process.exit(2)
}

// ---------------------------------------------------------------- vocabulary
// Mirrors ClassStereotype / RelationStereotype / Nature / AggregationKind in
// it.unibz.inf.ontouml.vp.model.ontouml.model. A value outside these is dropped by
// StereotypesManager.applyStereotype, so the element imports without its stereotype.

const CLASS_STEREOTYPES = new Set(['type', 'historicalRole', 'historicalRoleMixin', 'event',
  'situation', 'category', 'mixin', 'roleMixin', 'phaseMixin', 'kind', 'collective', 'quantity',
  'relator', 'quality', 'mode', 'subkind', 'role', 'phase', 'enumeration', 'datatype', 'abstract'])

const RELATION_STEREOTYPES = new Set(['material', 'derivation', 'comparative', 'mediation',
  'characterization', 'externalDependence', 'componentOf', 'memberOf', 'subCollectionOf',
  'subQuantityOf', 'instantiation', 'termination', 'participational', 'participation',
  'historicalDependence', 'creation', 'manifestation', 'bringsAbout', 'triggers'])

const NATURES = new Set(['functional-complex', 'collective', 'quantity', 'relator',
  'intrinsic-mode', 'extrinsic-mode', 'quality', 'event', 'situation', 'type', 'abstract'])

const PROPERTY_STEREOTYPES = new Set(['begin', 'end'])

const AGGREGATION_KINDS = new Set(['NONE', 'SHARED', 'COMPOSITE'])

// Types PackageDeserializer.deserializeContents() accepts; anything else is silently skipped.
const PACKAGEABLE = new Set(['Package', 'Class', 'Relation', 'Generalization', 'GeneralizationSet'])

/** Best-effort human label for an element, so a warning names the concept, not an index. */
const label = (node) => {
  const n = node?.name
  if (typeof n === 'string' && n) return ` "${n}"`
  if (n && typeof n === 'object') {
    const first = Object.values(n).find((v) => typeof v === 'string' && v)
    if (first) return ` "${first}"`
  }
  return node?.id ? ` <${node.id}>` : ''
}

const errors = []
const warnings = []
const err = (path, msg) => errors.push(`${path}: ${msg}`)
const warn = (path, msg) => warnings.push(`${path}: ${msg}`)

// ---------------------------------------------------------------- walk

const ids = new Map() // id -> type, for every element in the file
const references = [] // { path, field, id, expected: Set<type> }

function checkMultilingual(path, field, value) {
  if (value === null || value === undefined) return
  if (typeof value === 'string') return // MultilingualTextDeserializer accepts a bare string
  if (typeof value !== 'object' || Array.isArray(value)) {
    err(`${path}.${field}`, 'must be a string, an object of language→text, or null')
    return
  }
  for (const [lang, text] of Object.entries(value)) {
    if (typeof text !== 'string') err(`${path}.${field}.${lang}`, 'must be a string')
  }
}

function checkElement(path, node, expectedType) {
  if (node === null || typeof node !== 'object' || Array.isArray(node)) {
    err(path, 'must be an object')
    return false
  }
  if (node.type !== expectedType) {
    err(path, `"type" must be "${expectedType}", got ${JSON.stringify(node.type)}`)
    return false
  }
  if (typeof node.id !== 'string' || node.id === '') {
    // ElementDeserializer does root.get("id").asText() with no null check → NPE on import.
    err(path, '"id" must be a non-empty string')
    return false
  }
  if (ids.has(node.id)) {
    err(path, `duplicate id "${node.id}" (already used by a ${ids.get(node.id)})`)
  } else {
    ids.set(node.id, expectedType)
  }
  checkMultilingual(path, 'name', node.name)
  checkMultilingual(path, 'description', node.description)
  if (node.customProperties !== undefined) {
    warn(path, '"customProperties" is ignored by the plugin — it reads "propertyAssignments"')
  }
  return true
}

function checkReference(path, field, value, expected) {
  if (value === null || value === undefined) return
  if (typeof value === 'string') {
    err(`${path}.${field}`,
      `must be a reference object {"id", "type"}, not the bare id ${JSON.stringify(value)} ` +
      '(the plugin reads it as an object and would silently drop the link)')
    return
  }
  if (typeof value !== 'object' || Array.isArray(value)) {
    err(`${path}.${field}`, 'must be a reference object {"id", "type"}')
    return
  }
  if (typeof value.id !== 'string' || value.id === '') {
    err(`${path}.${field}.id`, 'must be a non-empty string')
    return
  }
  if (!expected.has(value.type)) {
    err(`${path}.${field}.type`,
      `must be one of ${[...expected].join(', ')}, got ${JSON.stringify(value.type)}`)
    return
  }
  references.push({ path: `${path}.${field}`, id: value.id, expected })
}

// `optional` suppresses the "no stereotype" warning for kinds that are normally bare
// (relation ends), where only «begin»/«end» ever apply.
function checkStereotype(path, node, vocabulary, kind, optional = false) {
  const value = node.stereotype ?? null
  if (value === null) {
    if (!optional) {
      warn(path, `${kind}${label(node)} has no stereotype — it imports as a plain UML element`)
    }
    return
  }
  if (typeof value !== 'string') {
    err(`${path}.stereotype`, 'must be a string or null')
    return
  }
  if (!vocabulary.has(value)) {
    err(`${path}.stereotype`,
      `${kind}${label(node)} carries "${value}", which is not an OntoUML ${kind} stereotype — ` +
      'the plugin drops it on import')
  }
}

function checkProperty(path, node) {
  if (!checkElement(path, node, 'Property')) return
  checkStereotype(path, node, PROPERTY_STEREOTYPES, 'property', true)
  checkReference(path, 'propertyType', node.propertyType, new Set(['Class', 'Relation']))
  if (node.propertyType === null || node.propertyType === undefined) {
    err(`${path}.propertyType`, 'missing — a relation end with no type makes the plugin ' +
      'throw "Source of the relation is null." while loading the model')
  }
  if (node.cardinality !== null && node.cardinality !== undefined) {
    if (typeof node.cardinality !== 'string' || !/^(\*|\d+|\d+\.\.(\d+|\*))$/.test(node.cardinality)) {
      err(`${path}.cardinality`,
        `must be null or a UML multiplicity ("1", "*", "0..1", "1..*"), got ${JSON.stringify(node.cardinality)}`)
    }
  }
  if (node.aggregationKind !== null && node.aggregationKind !== undefined
      && !AGGREGATION_KINDS.has(node.aggregationKind)) {
    err(`${path}.aggregationKind`,
      `must be null, ${[...AGGREGATION_KINDS].join(', ')}, got ${JSON.stringify(node.aggregationKind)}`)
  }
  for (const field of ['subsettedProperties', 'redefinedProperties']) {
    const list = node[field]
    if (list === null || list === undefined) continue
    if (!Array.isArray(list)) { err(`${path}.${field}`, 'must be an array or null'); continue }
    list.forEach((ref, i) =>
      checkReference(`${path}.${field}[${i}]`, 'ref', ref, new Set(['Property'])))
  }
}

function checkClassifierBody(path, node) {
  for (const flag of ['isAbstract', 'isDerived']) {
    if (node[flag] !== undefined && node[flag] !== null && typeof node[flag] !== 'boolean') {
      err(`${path}.${flag}`, 'must be a boolean')
    }
  }
  const props = node.properties
  if (props === null || props === undefined) return
  if (!Array.isArray(props)) {
    err(`${path}.properties`, 'must be an array of Property objects or null')
    return
  }
  props.forEach((p, i) => checkProperty(`${path}.properties[${i}]`, p))
}

function checkClass(path, node) {
  if (!checkElement(path, node, 'Class')) return
  checkStereotype(path, node, CLASS_STEREOTYPES, 'class')
  checkClassifierBody(path, node)

  const restrictedTo = node.restrictedTo
  if (Array.isArray(restrictedTo)) {
    restrictedTo.forEach((n, i) => {
      if (!NATURES.has(n)) err(`${path}.restrictedTo[${i}]`, `"${n}" is not an OntoUML nature`)
    })
  } else if (restrictedTo !== null && restrictedTo !== undefined) {
    err(`${path}.restrictedTo`, 'must be an array of natures or null')
  }

  if (typeof node.order === 'string') {
    // ClassDeserializer uses canConvertToInt(), which is false for a JSON string.
    err(`${path}.order`,
      `must be a number, not the string ${JSON.stringify(node.order)} (the plugin reads it as null)`)
  } else if (node.order !== null && node.order !== undefined && !Number.isInteger(node.order)) {
    err(`${path}.order`, 'must be an integer or null')
  }

  if (node.literals !== null && node.literals !== undefined) {
    if (!Array.isArray(node.literals)) err(`${path}.literals`, 'must be an array or null')
    else node.literals.forEach((l, i) => checkElement(`${path}.literals[${i}]`, l, 'Literal'))
  }
}

function checkRelation(path, node) {
  if (!checkElement(path, node, 'Relation')) return
  checkStereotype(path, node, RELATION_STEREOTYPES, 'relation')
  checkClassifierBody(path, node)

  const props = Array.isArray(node.properties) ? node.properties : []
  if (props.length < 2) {
    err(`${path}.properties`,
      `a relation needs at least 2 ends, got ${props.length} — the plugin skips it as not ` +
      'holding between classes')
  }
}

function checkGeneralization(path, node) {
  if (!checkElement(path, node, 'Generalization')) return
  const classifier = new Set(['Class', 'Relation'])
  checkReference(path, 'general', node.general, classifier)
  checkReference(path, 'specific', node.specific, classifier)
  for (const field of ['general', 'specific']) {
    if (node[field] === null || node[field] === undefined) {
      err(`${path}.${field}`, 'missing — the generalization imports with no end')
    }
  }
  if (node.general?.id && node.general.id === node.specific?.id) {
    err(path, 'general and specific are the same element')
  }
}

function checkGeneralizationSet(path, node) {
  if (!checkElement(path, node, 'GeneralizationSet')) return
  checkReference(path, 'categorizer', node.categorizer, new Set(['Class']))
  const gens = node.generalizations
  if (!Array.isArray(gens) || gens.length === 0) {
    err(`${path}.generalizations`, 'must be a non-empty array of Generalization references')
    return
  }
  gens.forEach((g, i) =>
    checkReference(`${path}.generalizations[${i}]`, 'ref', g, new Set(['Generalization'])))
}

function checkPackage(path, node) {
  if (!checkElement(path, node, 'Package')) return
  const contents = node.contents
  if (contents === null || contents === undefined) {
    warn(path, '"contents" is null — this package imports empty')
    return
  }
  if (!Array.isArray(contents)) {
    err(`${path}.contents`, 'must be an array of nested elements or null')
    return
  }
  contents.forEach((child, i) => {
    const childPath = `${path}.contents[${i}]`
    if (child === null || typeof child !== 'object' || Array.isArray(child)) {
      err(childPath, 'must be an object')
      return
    }
    if (typeof child.id === 'string' && Object.keys(child).length <= 2) {
      err(childPath, 'contents must hold full nested elements, not {"id", "type"} references')
      return
    }
    switch (child.type) {
      case 'Package': checkPackage(childPath, child); break
      case 'Class': checkClass(childPath, child); break
      case 'Relation': checkRelation(childPath, child); break
      case 'Generalization': checkGeneralization(childPath, child); break
      case 'GeneralizationSet': checkGeneralizationSet(childPath, child); break
      case 'BinaryRelation':
      case 'NaryRelation':
        err(childPath, `"type": ${JSON.stringify(child.type)} is not read by the plugin — ` +
          'use "Relation" (the plugin\'s content switch drops anything else without a warning)')
        break
      default:
        err(childPath, `"type": ${JSON.stringify(child.type)} is not packageable — expected ` +
          `one of ${[...PACKAGEABLE].join(', ')}`)
    }
  })
}

// ---------------------------------------------------------------- entry

if (instance === null || typeof instance !== 'object' || Array.isArray(instance)) {
  console.error('✗ INVALID — the file must contain a single JSON object')
  process.exit(1)
}

if (instance.elements !== undefined || instance.root !== undefined) {
  console.error('✗ INVALID — this is a flat "OntoUML Schema v1.0.2" file (`elements` + `root`).')
  console.error('  The Visual Paradigm plugin reads a nested project: `model` (one Package whose')
  console.error('  `contents` holds the full elements) plus `diagrams`. Its ProjectDeserializer')
  console.error('  never looks at `elements`/`root`, so this file imports as an empty project.')
  if (withSchema) await runSchema(instance)
  process.exit(1)
}

checkElement('project', instance, 'Project')
if (instance.model === null || instance.model === undefined) {
  err('project.model', 'missing — the plugin imports an empty project')
} else {
  checkPackage('project.model', instance.model)
}
if (instance.diagrams !== null && instance.diagrams !== undefined
    && !Array.isArray(instance.diagrams)) {
  err('project.diagrams', 'must be an array or null')
}

// Referential integrity — the rule ReferenceResolver enforces by throwing.
for (const ref of references) {
  const targetType = ids.get(ref.id)
  if (targetType === undefined) {
    err(ref.path, `points at "${ref.id}", which is not defined anywhere in the file ` +
      '(the plugin aborts the whole import: "Referenced element ... does not exist!")')
  } else if (!ref.expected.has(targetType)) {
    err(ref.path, `points at "${ref.id}", which is a ${targetType}, not ` +
      `${[...ref.expected].join('/')}`)
  }
}

if (withSchema) await runSchema(instance)

for (const w of warnings.slice(0, 40)) console.error(`  ! ${w}`)
if (warnings.length > 40) console.error(`  ! ...and ${warnings.length - 40} more warning(s)`)

if (errors.length === 0) {
  const counts = {}
  for (const t of ids.values()) counts[t] = (counts[t] ?? 0) + 1
  const summary = Object.entries(counts).map(([t, n]) => `${n} ${t}`).join(', ')
  console.log(`✓ VALID — ${instancePath} imports into the Visual Paradigm OntoUML plugin`)
  console.log(`  ${summary}${warnings.length ? ` — ${warnings.length} warning(s)` : ''}`)
  process.exit(0)
}

console.error(`✗ INVALID — ${errors.length} error(s):`)
for (const e of errors.slice(0, 40)) console.error(`  ${e}`)
if (errors.length > 40) console.error(`  ...and ${errors.length - 40} more`)
process.exit(1)

// ---------------------------------------------------------------- optional schema pass

async function runSchema(doc) {
  const { default: Ajv2020 } = await import('ajv/dist/2020.js')
  const { default: addFormats } = await import('ajv-formats')
  const schema = JSON.parse(readFileSync(resolve(here, 'ontouml-schema.json'), 'utf8'))
  const ajv = new Ajv2020({ allErrors: true, strict: false })
  addFormats(ajv)
  const validate = ajv.compile(schema)
  if (validate(doc)) {
    console.error('  (schema) conforms to the w3id OntoUML Schema v1.0.2')
    return
  }
  // A oneOf-heavy schema reports every failed branch; the deepest paths are the useful ones.
  const deepest = [...validate.errors].sort(
    (a, b) => b.instancePath.length - a.instancePath.length).slice(0, 10)
  console.error(`  (schema) does NOT conform to the w3id OntoUML Schema v1.0.2 ` +
    `(${validate.errors.length} error(s)); deepest:`)
  for (const e of deepest) console.error(`    ${e.instancePath || '/'} ${e.message}`)
}
