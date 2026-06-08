import { Box, Image, Text, VStack } from '@chakra-ui/react'
import type { DiagramView } from '../types/api'
import HtmlContent from './HtmlContent'

interface Props {
  diagram: DiagramView
}

export default function DiagramWithMap({ diagram }: Props) {
  return (
    <VStack align="stretch" gap={4} my={6}>
      <Text fontSize="lg">
        Figure {diagram.figureNumber} presents the {diagram.introText}.
      </Text>

      <Box textAlign="center">
        {diagram.simpleImage ? (
          <Image
            src={diagram.imageSrc}
            alt={diagram.labelText}
            maxW="100%"
            mx="auto"
          />
        ) : (
          <>
            <Image
              src={diagram.imageSrc}
              alt={`Diagram of ${diagram.mapName}`}
              width={diagram.imageWidth ? `${diagram.imageWidth}px` : undefined}
              maxW="100%"
              mx="auto"
              useMap={`#${diagram.mapName}`}
            />
            <map name={diagram.mapName}>
              {diagram.mapAreas.map((area, i) => (
                <area
                  key={i}
                  shape="rect"
                  coords={area.coords}
                  href={area.href}
                  target={area.target || undefined}
                  alt={area.alt}
                />
              ))}
            </map>
          </>
        )}
        <Text fontWeight="bold" mt={4}>
          Figure {diagram.figureNumber}. {diagram.labelText}.
        </Text>
      </Box>

      {diagram.description && (
        <HtmlContent fontSize="lg" html={diagram.description} />
      )}
    </VStack>
  )
}
