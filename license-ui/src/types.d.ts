export type License = {
  id: string;
  key: string;
  audience: string;
  active: boolean;
  // optional:
  createdAt?: string;
  expiresAt?: string;
};

