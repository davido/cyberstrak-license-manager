import React, { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import type { License } from "../types";

import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import Paper from "@mui/material/Paper";
import Stack from "@mui/material/Stack";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";

import AddIcon from "@mui/icons-material/Add";
import VisibilityIcon from "@mui/icons-material/Visibility";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { useNavigate } from "react-router-dom";
import ConfirmDialog from "../components/ConfirmDialog";
import { useSnackbar } from "notistack";
import AppLayout from "../components/AppLayout";

const LicenseListPage: React.FC = () => {
  const [items, setItems] = useState<License[]>([]);
  const [query, setQuery] = useState("");
  const [confirmId, setConfirmId] = useState<string | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();

  const load = async () => {
    const res = await api.get<License[]>("/licenses"); // optional: /licenses?q=query
    setItems(res.data);
  };

  useEffect(() => { load(); }, []);

  const filtered = useMemo(() => {
    const q = query.toLowerCase();
    return items.filter(i =>
      i.key.toLowerCase().includes(q) ||
      i.aud.toLowerCase().includes(q) ||
      (i.id && i.id.toString().includes(q))
    );
  }, [items, query]);

  const onDelete = async (id: string) => {
    await api.delete(`/licenses/${id}`);
    enqueueSnackbar("Lizenz gelöscht", { variant: "success" });
    setConfirmId(null);
    load();
  };

  return (
    <AppLayout>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Lizenzen</Typography>
        <Button startIcon={<AddIcon />} variant="contained" onClick={() => navigate("/licenses/new")}>
          Neu
        </Button>
      </Stack>

      <TextField
        fullWidth placeholder="Suche (Key, Audience, ID)…"
        value={query} onChange={(e) => setQuery(e.target.value)} sx={{ mb: 2 }}
      />

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Key</TableCell>
              <TableCell>Audience</TableCell>
              <TableCell>Aktiv</TableCell>
              <TableCell align="right">Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.map((row) => (
              <TableRow key={row.id} hover>
                <TableCell>{row.id}</TableCell>
                <TableCell>{row.key}</TableCell>
                <TableCell>{row.aud}</TableCell>
                <TableCell>{row.active ? "Ja" : "Nein"}</TableCell>
                <TableCell align="right">
                  <IconButton onClick={() => navigate(`/licenses/${row.id}`)}><VisibilityIcon /></IconButton>
                  <IconButton onClick={() => navigate(`/licenses/${row.id}/edit`)}><EditIcon /></IconButton>
                  <IconButton color="error" onClick={() => setConfirmId(row.id)}><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow><TableCell colSpan={5}><Box p={2}>Keine Einträge</Box></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <ConfirmDialog
        open={!!confirmId}
        title="Lizenz löschen?"
        content="Diese Aktion kann nicht rückgängig gemacht werden."
        onCancel={() => setConfirmId(null)}
        onConfirm={() => confirmId && onDelete(confirmId)}
      />
    </AppLayout>
  );
};

export default LicenseListPage;

