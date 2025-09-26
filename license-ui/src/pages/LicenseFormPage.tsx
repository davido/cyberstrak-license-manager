import React, { useEffect, useState } from "react";
import { api } from "../api/client";
import type { License } from "../types";
import { useNavigate, useParams } from "react-router-dom";

import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Checkbox from "@mui/material/Checkbox";
import Container from "@mui/material/Container";
import FormControlLabel from "@mui/material/FormControlLabel";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import { useSnackbar } from "notistack";

type Props = { mode: "create" | "edit" };

const LicenseFormPage: React.FC<Props> = ({ mode }) => {
  const { id } = useParams();
  const isEdit = mode === "edit";
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();
  const [form, setForm] = useState<License>({ id: "", key: "", audience: "", active: true });

  useEffect(() => {
    if (isEdit && id) {
      (async () => {
        const res = await api.get<License>(`/licenses/${id}`);
        setForm(res.data);
      })();
    }
  }, [isEdit, id]);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (isEdit) {
      await api.put(`/licenses/${id}`, form);
      enqueueSnackbar("Lizenz aktualisiert", { variant: "success" });
      navigate(`/licenses/${id}`);
    } else {
      const res = await api.post<License>("/licenses", form);
      enqueueSnackbar("Lizenz angelegt", { variant: "success" });
      navigate(`/licenses/${res.data.id}`);
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          {isEdit ? `Lizenz bearbeiten #${id}` : "Neue Lizenz anlegen"}
        </Typography>

        <Box component="form" onSubmit={onSubmit} display="grid" gap={2}>
          <TextField
            label="Key" value={form.key} required
            onChange={(e) => setForm({ ...form, key: e.target.value })}
          />
          <TextField
            label="Audience" value={form.audience} required
            onChange={(e) => setForm({ ...form, audience: e.target.value })}
          />
          <FormControlLabel
            control={<Checkbox checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />}
            label="Aktiv"
          />
          <Stack direction="row" spacing={1}>
            <Button variant="outlined" onClick={() => navigate(-1)}>Abbrechen</Button>
            <Button variant="contained" type="submit">{isEdit ? "Speichern" : "Anlegen"}</Button>
          </Stack>
        </Box>
      </Paper>
    </Container>
  );
};

export default LicenseFormPage;

