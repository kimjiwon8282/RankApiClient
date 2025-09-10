document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const response = await fetch('/api/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({email, password})
    });
    if (response.ok) {
        const { accessToken } = await response.json();
        localStorage.setItem('access_token', accessToken);
        alert('Login successful');
        window.location.href = '/home';
    } else {
        alert('Login failed');
    }
});