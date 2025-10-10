import React, { type PropsWithChildren } from "react";

import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Tooltip from "@mui/material/Tooltip";

import LogoutIcon from "@mui/icons-material/Logout";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";
import Container from "@mui/material/Container";

const AppLayout: React.FC<PropsWithChildren> = ({children}) => {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <Typography
            variant="h6"
            component="div"
            sx={{ flexGrow: 1, cursor: "pointer" }}
            onClick={() => navigate("/licenses")}
          >
            License Manager
          </Typography>

          {user?.sub && (
            <Typography variant="body1" sx={{ mr: 2 }}>
              {user.sub}
            </Typography>
          )}

          <Tooltip title="Logout">
            <IconButton color="inherit" onClick={handleLogout}>
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      <Box component="main" sx={{ p: 2 }}>
        <Container maxWidth="xl" sx={{ mt: 4 }}>
          {children}
        </Container>
      </Box>
    </Box>
  );
};

export default AppLayout;

