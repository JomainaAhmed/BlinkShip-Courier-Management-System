/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#FF6B00', // Safety Orange
          dark: '#E66000',
        },
        background: '#000000', // Absolute Black
        card: '#1A1A1A', // Dark Gray
        surface: {
          light: '#ffffff',
          dim: '#0D0D0D',
          dark: '#000000',
        },
        neutral: '#FFFFFF', // Updated to pure white for better opacity control on dark backgrounds
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        display: ['Inter', 'sans-serif'], // Behance uses geometric sans for all
      },
      borderRadius: {
        '3xl': '32px',
        '4xl': '48px',
      },
      boxShadow: {
        'premium': '0 20px 40px -15px rgba(255, 107, 0, 0.15)',
        'glass': '0 8px 32px 0 rgba(0, 0, 0, 0.8)',
      },
      backdropBlur: {
        xs: '2px',
      },
      fontSize: {
        'xs': ['0.8125rem', { lineHeight: '1.25rem' }], // 13px -> 14.6px
        'sm': ['0.9375rem', { lineHeight: '1.5rem' }],  // 15px -> 16.9px
      }
    },
  },
  plugins: [],
}
