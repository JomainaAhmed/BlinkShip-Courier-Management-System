export interface User {
  id?: number;
  username: string;
  email?: string;
  password?: string;
  role?: string;
  name?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  user?: User;
}
