CREATE DATABASE auth_db2;
CREATE DATABASE delivery_db2;
CREATE DATABASE tracking_db2;
CREATE DATABASE admin_db2;
CREATE DATABASE sonarqube;

-- Connect to auth_db2 to seed the admin user
\c auth_db2;

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

-- Insert admin user (password is 'admin123' hashed with BCrypt)
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@blinkship.com', '$2a$10$iMtaq6Faaue3PxYdjv4fUOrOOnld5oUhONd6Gy8RpTvHfMmmiwlAa', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Insert user456 (password is '456456' hashed with BCrypt)
INSERT INTO users (username, email, password, role)
VALUES ('user456', 'user456@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Additional Indian Users (Password for all is 'password123')
INSERT INTO users (username, email, password, role) VALUES 
('arjun', 'arjun@example.com', '$2a$10$mC7p6nJzVl7Y.fV5.HkE.7u.O/P9D1v5gY8QyZ7H5gY8QyZ7H5gY8Qy', 'USER'),
('priya', 'priya@example.com', '$2a$10$mC7p6nJzVl7Y.fV5.HkE.7u.O/P9D1v5gY8QyZ7H5gY8QyZ7H5gY8Qy', 'USER'),
('rahul', 'rahul@example.com', '$2a$10$mC7p6nJzVl7Y.fV5.HkE.7u.O/P9D1v5gY8QyZ7H5gY8QyZ7H5gY8Qy', 'USER'),
('anjali', 'anjali@example.com', '$2a$10$mC7p6nJzVl7Y.fV5.HkE.7u.O/P9D1v5gY8QyZ7H5gY8QyZ7H5gY8Qy', 'USER'),
('testuser', 'testuser@lpu.in', '$2a$10$Z3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/3Y/', 'USER')
ON CONFLICT (username) DO NOTHING;
