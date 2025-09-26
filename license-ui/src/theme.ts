import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#1976d2", // Standard-MUI-Blau
    },
    secondary: {
      main: "#9c27b0", // optional: Lila
    },
  },
  shape: {
    borderRadius: 12,
  },
});

