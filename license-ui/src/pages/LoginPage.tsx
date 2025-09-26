import React, { useState } from "react";
import Avatar from "@mui/material/Avatar";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import Container from "@mui/material/Container";
import TextField from "@mui/material/TextField";
import Typography from "@mui/material/Typography";
import Paper from "@mui/material/Paper";
import LockIcon from "@mui/icons-material/Lock";
import { useAuth } from "../auth/AuthContext";
import { useNavigate, useLocation } from "react-router-dom";

const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const location = useLocation() as any;
  const from = location.state?.from?.pathname || "/licenses";

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (e: any) {
      setError(e?.response?.data?.message || "Login fehlgeschlagen");
    } finally {
      setBusy(false);
    }
  };

  return (
    <Container maxWidth="xs" sx={{ mt: 8 }}>
      <Paper elevation={3} sx={{ p: 3 }}>
        <Box display="flex" flexDirection="column" alignItems="center" gap={2}>
          <Avatar><LockIcon /></Avatar>
          <Typography variant="h5">Login</Typography>
          <Box component="form" onSubmit={onSubmit} width="100%">
            <TextField label="Benutzername" fullWidth margin="normal"
              value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
            <TextField label="Passwort" type="password" fullWidth margin="normal"
              value={password} onChange={(e) => setPassword(e.target.value)} />
            {error && <Typography color="error" variant="body2">{error}</Typography>}
            <Button type="submit" variant="contained" fullWidth disabled={busy} sx={{ mt: 2 }}>
              {busy ? "Bitte warten…" : "Einloggen"}
            </Button>
          </Box>
        </Box>
      </Paper>
    </Container>
  );
};

export default LoginPage;

