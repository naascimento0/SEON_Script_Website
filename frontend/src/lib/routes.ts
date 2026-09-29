/**
 * The backend emits app-relative links ("/ontology/CPO#CPO_Code+Development"). The deploy can
 * live under a context path (e.g. /seon2), which React Router knows as its basename — so router
 * navigation takes those paths as they are, while a raw `href` needs the base spelled out.
 */
const BASE = import.meta.env.BASE_URL.replace(/\/$/, '')

/** An app route, as opposed to a same-page anchor ("#x") or an off-site URL. */
export function isAppRoute(href: string | null | undefined): href is string {
  return !!href && href.startsWith('/')
}

/** Same href with the deploy base, so middle-click and "open in new tab" land on the right page. */
export function withBase(href: string): string {
  return isAppRoute(href) ? BASE + href : href
}

/** True when the click should be left to the browser (new tab, new window, download…). */
export function isModifiedClick(e: {
  button: number
  metaKey: boolean
  ctrlKey: boolean
  shiftKey: boolean
  altKey: boolean
}): boolean {
  return e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey
}
