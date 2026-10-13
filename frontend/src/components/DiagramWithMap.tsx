import { useEffect, useRef, useState, type MouseEvent } from 'react'
import { Box, Image, Text, VStack } from '@chakra-ui/react'
import { useNavigate } from 'react-router-dom'
import type { DiagramView } from '../types/api'
import { isAppRoute, isModifiedClick, withBase } from '../lib/routes'
import HtmlContent from './HtmlContent'

interface Props {
  diagram: DiagramView
}

/**
 * `<area coords>` are absolute pixels in the diagram's own coordinate space — the
 * Astah bound rect, whose width is `imageWidth`. The browser does NOT rescale them
 * when CSS shrinks the image, so on a narrow screen (or whenever the diagram is
 * wider than the page container) every hotspot lands on the wrong concept. We
 * watch the rendered width and recompute the coordinates ourselves.
 */
function useMapScale(diagram: DiagramView) {
  const imgRef = useRef<HTMLImageElement>(null)
  const [scale, setScale] = useState(1)

  useEffect(() => {
    const img = imgRef.current
    if (!img || diagram.simpleImage) return

    const update = () => {
      const reference = diagram.imageWidth || img.naturalWidth
      const rendered = img.getBoundingClientRect().width
      if (!reference || !rendered) return
      setScale(rendered / reference)
    }

    update()
    const observer = new ResizeObserver(update)
    observer.observe(img)
    img.addEventListener('load', update)

    return () => {
      observer.disconnect()
      img.removeEventListener('load', update)
    }
  }, [diagram.imageSrc, diagram.imageWidth, diagram.simpleImage])

  return { imgRef, scale }
}

function scaleCoords(coords: string, scale: number): string {
  if (scale === 1) return coords
  const parts = coords.split(',').map(Number)
  if (parts.some(Number.isNaN)) return coords
  return parts.map((n) => Math.round(n * scale)).join(',')
}

export default function DiagramWithMap({ diagram }: Props) {
  const { imgRef, scale } = useMapScale(diagram)
  const navigate = useNavigate()

  /** Keeps a hotspot pointing at another ontology inside the SPA instead of reloading the page. */
  const handleAreaClick = (e: MouseEvent<HTMLAreaElement>, href: string) => {
    if (!isAppRoute(href) || isModifiedClick(e)) return
    e.preventDefault()
    navigate(href)
  }

  return (
    <VStack align="stretch" gap={4} my={6}>
      <Text fontSize="lg">
        Figure {diagram.figureNumber} presents the {diagram.introText}.
      </Text>

      <Box textAlign="center">
        {diagram.simpleImage ? (
          <Image
            src={withBase(diagram.imageSrc)}
            alt={diagram.labelText}
            maxW="100%"
            mx="auto"
          />
        ) : (
          <>
            <Image
              ref={imgRef}
              src={withBase(diagram.imageSrc)}
              alt={`Diagram of ${diagram.mapName}`}
              width={diagram.imageWidth ? `${diagram.imageWidth}px` : undefined}
              maxW="100%"
              height="auto"
              mx="auto"
              useMap={`#${diagram.mapName}`}
            />
            <map name={diagram.mapName}>
              {diagram.mapAreas.map((area, i) => (
                <area
                  key={i}
                  shape="rect"
                  coords={scaleCoords(area.coords, scale)}
                  href={withBase(area.href)}
                  onClick={(e) => handleAreaClick(e, area.href)}
                  target={area.target || undefined}
                  alt={area.alt}
                  title={area.alt || undefined}
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
