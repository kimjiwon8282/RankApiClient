const token = searchParam('token');
if(token){
    localStorage.setItem("access_token",token);
    window.history.replaceState({}, document.title, window.location.pathname);
}
function searchParam(key){
    return new URLSearchParams(location.search).get(key);
}