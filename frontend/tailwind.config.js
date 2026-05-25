/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      animation: {
        'shiny-text': 'shiny-text 8s linear infinite',
      },
      keyframes: {
        'shiny-text': {
          '0%, 90%, 100%': { 'background-position': '200% 0' },
          '30%, 60%': { 'background-position': '-200% 0' },
        },
      },
    },
  },
  plugins: [],
}
