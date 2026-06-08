import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import PublicationsPage from './pages/PublicationsPage'
import LoginPage from './pages/LoginPage'
import UploadPage from './pages/UploadPage'
import OntologyPage from './pages/OntologyPage'
import NotFoundPage from './pages/NotFoundPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="publications" element={<PublicationsPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="upload" element={<UploadPage />} />
        <Route path="ontology/:name" element={<OntologyPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
