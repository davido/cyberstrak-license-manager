import React, { Suspense, lazy } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import RequireAuth from "./auth/RequireAuth";
import AppLayout from "./components/AppLayout";

// Lazy-loaded Pages
const LoginPage = lazy(() => import("./pages/LoginPage"));
const LicenseListPage = lazy(() => import("./pages/LicenseListPage"));
const LicenseDetailPage = lazy(() => import("./pages/LicenseDetailPage"));
const LicenseFormPage = lazy(() => import("./pages/LicenseFormPage"));

const App: React.FC = () => {
  return (
    <Suspense fallback={<div>Loading…</div>}>
      <Routes>
        <Route path="/" element={<Navigate to="/licenses" replace />} />
        <Route path="/login" element={<LoginPage />} />

        <Route element={<RequireAuth />}>
          <Route element={<AppLayout />}>
            <Route path="/licenses" element={<LicenseListPage />} />
            <Route path="/licenses/new" element={<LicenseFormPage mode="create" />} />
            <Route path="/licenses/:id" element={<LicenseDetailPage />} />
            <Route path="/licenses/:id/edit" element={<LicenseFormPage mode="edit" />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/licenses" replace />} />
      </Routes>
    </Suspense>
  );
};

export default App;

