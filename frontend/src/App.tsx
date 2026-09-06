import { Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import PublicationsPage from './pages/PublicationsPage'
import LoginPage from './pages/LoginPage'
import UploadPage from './pages/UploadPage'
import OntologiesPage from './pages/OntologiesPage'
import OntologyPage from './pages/OntologyPage'
import SuggestionsPage from './pages/SuggestionsPage'
import NotFoundPage from './pages/NotFoundPage'

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="publications" element={<PublicationsPage />} />
        <Route path="ontologies" element={<OntologiesPage />} />
        <Route path="suggestions" element={<SuggestionsPage />} />
        <Route path="login" element={<LoginPage />} />
        <Route path="upload" element={<UploadPage />} />
        <Route path="ontology/:name" element={<OntologyPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
