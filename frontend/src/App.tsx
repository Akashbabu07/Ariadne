import { Routes, Route } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import AppShell from "./components/layout/AppShell";
import ProtectedRoute from "./components/ProtectedRoute";
import RepositoriesPage from "./pages/RepositoriesPage";
import RepositoryDetailPage from "./pages/RepositoryDetailPage";
import { Toaster } from "./components/Toaster";

export default function App() {
  return (
      <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppShell />}>
              <Route path="/repositories/:repositoryId" element={<RepositoryDetailPage />} />
            <Route path="/" element={<RepositoriesPage />} />
            {/* /search, /ask, and /repositories/:id land in F2+ */}
          </Route>
        </Route>
      </Routes>
    <Toaster />
    </>
  );
}