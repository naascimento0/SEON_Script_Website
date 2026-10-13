import axios from 'axios'

export const api = axios.create({
  // Under a context path (e.g. /seon2) every call must carry it; BASE_URL is set at build time.
  baseURL: import.meta.env.BASE_URL,
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})
