import React from "react";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";

type Props = {
  open: boolean;
  title: string;
  content?: string;
  onConfirm: () => void;
  onCancel: () => void;
};

const ConfirmDialog: React.FC<Props> = ({ open, title, content, onConfirm, onCancel }) => (
  <Dialog open={open} onClose={onCancel}>
    <DialogTitle>{title}</DialogTitle>
    {content && (
      <DialogContent>
        <DialogContentText>{content}</DialogContentText>
      </DialogContent>
    )}
    <DialogActions>
      <Button onClick={onCancel}>Abbrechen</Button>
      <Button color="error" variant="contained" onClick={onConfirm}>Löschen</Button>
    </DialogActions>
  </Dialog>
);

export default ConfirmDialog;

