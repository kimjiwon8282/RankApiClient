export default {
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