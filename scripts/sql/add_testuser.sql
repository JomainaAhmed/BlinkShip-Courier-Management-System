INSERT INTO users (username, email, password, role) 
VALUES ('testuser', 'testuser@lpu.in', '$2a$10$iMtaq6Faaue3PxYdjv4fUOrOOnld5oUhONd6Gy8RpTvHfMmmiwlAa', 'USER')
ON CONFLICT (username) DO NOTHING;
