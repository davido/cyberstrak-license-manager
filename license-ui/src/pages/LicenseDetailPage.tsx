import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api } from "../api/client";
import type { License } from "../types";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";

const LicenseDetailPage: React.FC = () => {
  const { id } = useParams();
  const [lic, setLic] = useState<License | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      const res = await api.get<License>(`/licenses/${id}`);
      setLic(res.data);
    })();
  }, [id]);

  if (!lic) return null;

  return (
    <Container maxWidth="sm" sx={{ mt: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>Lizenz: {lic.id}</Typography>
        <Box display="grid" gap={1} sx={{ "& b": { mr: 1 } }}>
          <div><b>Key:</b> {lic.key}</div>
          <div><b>Audience:</b> {lic.aud}</div>
          <div><b>Aktiv:</b> {lic.active ? "Ja" : "Nein"}</div>
        </Box>
        <Stack direction="row" spacing={1} sx={{ mt: 2 }}>
          <Button variant="outlined" onClick={() => navigate("/licenses")}>Zurück</Button>
          <Button variant="contained" onClick={() => navigate(`/licenses/${lic.id}/edit`)}>Bearbeiten</Button>
        </Stack>
      </Paper>
    </Container>
  );
};

export default LicenseDetailPage;

