export interface User {
  id?: number;
  username: string;
  email?: string;
  password?: string;
  role?: string;
  name?: string; // Keep for compatibility if needed
}

export interface AuthResponse {
  token: string;
  user: User;
}
