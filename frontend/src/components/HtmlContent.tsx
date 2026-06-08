import { Box, type BoxProps } from '@chakra-ui/react'

interface Props extends BoxProps {
  html: string
}

export default function HtmlContent({ html, ...rest }: Props) {
  return <Box {...rest} dangerouslySetInnerHTML={{ __html: html }} />
}
