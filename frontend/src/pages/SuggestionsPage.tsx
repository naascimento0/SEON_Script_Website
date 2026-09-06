import { Box, Heading, Link, Text, VStack } from '@chakra-ui/react'

// ── Google Form configuration ────────────────────────────────────────────────
// Paste your form URLs below. In Google Forms, click "Send" → "<>" (embed) to get
// the embed URL (it ends with `?embedded=true`); the "link" tab gives the share URL.
const GOOGLE_FORM_EMBED_URL =
  'https://docs.google.com/forms/d/e/1FAIpQLSe9i2pk30FjfJ4e03U99RfNTXt9byzMMDVSU6J9VZ2B3WrowA/viewform?embedded=true'
const GOOGLE_FORM_SHARE_URL =
  'https://docs.google.com/forms/d/e/1FAIpQLSe9i2pk30FjfJ4e03U99RfNTXt9byzMMDVSU6J9VZ2B3WrowA/viewform'
// ─────────────────────────────────────────────────────────────────────────────

const isConfigured = !GOOGLE_FORM_EMBED_URL.includes('REPLACE_WITH_FORM_ID')

export default function SuggestionsPage() {
  return (
    <VStack align="stretch" gap={8}>
      <Box bg="bg.subtle" p={8} borderRadius="md" textAlign="center">
        <Heading size="3xl">Suggestions & Feedback</Heading>
        <Text fontSize="lg" mt={3}>
          Questions, suggestions, or issues about the SEON network are welcome. Your
          input helps us improve the ontologies and the site.
        </Text>
      </Box>

      {isConfigured ? (
        <Box>
          <iframe
            src={GOOGLE_FORM_EMBED_URL}
            title="SEON suggestions form"
            style={{ width: '100%', height: '1200px', border: 0 }}
            loading="lazy"
          />
          <Text fontSize="sm" color="fg.muted" mt={3}>
            Having trouble with the embedded form?{' '}
            <Link href={GOOGLE_FORM_SHARE_URL} target="_blank" color="fg">
              Open it in a new tab
            </Link>
            .
          </Text>
        </Box>
      ) : (
        <Box borderWidth="1px" borderRadius="md" p={6}>
          <Text>
            The feedback form is not configured yet. Set{' '}
            <code>GOOGLE_FORM_EMBED_URL</code> in <code>SuggestionsPage.tsx</code>.
          </Text>
        </Box>
      )}
    </VStack>
  )
}
