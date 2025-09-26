export type License = {
  id: string;
  key: string;
  aud: string;
  active: boolean;
  // optional:
  createdAt?: string;
  expiresAt?: string;
};

