import { resolve } from 'path';

export default {
    build: {
        outDir: 'dist',
        emptyOutDir: true,
        rollupOptions: {
            input: {
                index: resolve(__dirname, 'index.html'),
                login: resolve(__dirname, 'login.html'),
                signup: resolve(__dirname, 'signup.html'),
                home: resolve(__dirname, 'home.html'),
                optimizeProductName: resolve(__dirname, 'optimize-product-name.html'),
                myHistory: resolve(__dirname, 'my-history.html'),
                adminKeywordAnalysis: resolve(__dirname, 'admin/keyword-analysis.html')
            }
        }
    },

    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            '/ai': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            '/oauth2': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            '/login/oauth2': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            '/logout': {
                target: 'http://localhost:8080',
                changeOrigin: true
            },
            '/signup': {
                target: 'http://localhost:8080',
                changeOrigin: true
            }
        }
    }
};