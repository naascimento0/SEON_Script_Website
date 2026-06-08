import { Box, Heading, Separator, VStack } from '@chakra-ui/react'
import type { SectionView } from '../types/api'
import DiagramWithMap from './DiagramWithMap'
import HtmlContent from './HtmlContent'

interface Props {
  section: SectionView
}

type ChakraHeadingSize = 'lg' | 'md' | 'sm' | 'xs'
const HEADING_SIZE: Record<number, ChakraHeadingSize> = {
  3: 'lg',
  4: 'md',
  5: 'sm',
  6: 'xs',
}

export default function Section({ section }: Props) {
  const level = Math.min(Math.max(section.headingLevel, 3), 6)
  const asTag = `h${level}` as 'h3' | 'h4' | 'h5' | 'h6'

  return (
    <>
      <Separator my={8} />
      <Box py={4}>
        <Heading as={asTag} id={section.sectionRef} size={HEADING_SIZE[level] ?? 'sm'}>
          {section.sectionNumber} {section.sectionName}
        </Heading>

        {section.description && (
          <HtmlContent fontSize="lg" mt={4} html={section.description} />
        )}

        {section.diagrams.length > 0 && (
          <VStack align="stretch" gap={6} mt={4}>
            {section.diagrams.map((d) => (
              <DiagramWithMap key={d.figureNumber} diagram={d} />
            ))}
          </VStack>
        )}
      </Box>

      {section.subsections.map((sub) => (
        <Section key={sub.sectionRef} section={sub} />
      ))}
    </>
  )
}
