// js/common.js

// 1. 全局配置 (API 接口地址)
window.APP_CONFIG = {
    BASE_URL: 'http://127.0.0.1:8091',
    ALIPAY_BASE_URL: 'http://127.0.0.1:7860'
};

// 2. 通用工具函数 - 获取 Cookie
window.getCookie = function(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for(let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
    return null;
};

// 3. 通用工具函数 - 动态文字头像生成
window.getAvatarDataURI = function(name) {
    if (!name) name = "U";
    const char = name.charAt(0).toUpperCase();
    const colors = ['#2b80ff', '#ff9f43', '#00cec9', '#6c5ce7', '#00b894', '#e84393'];
    const color = colors[char.charCodeAt(0) % colors.length] || colors[0];
    const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect width="100" height="100" fill="${color}" rx="25"/><text x="50%" y="50%" font-size="45" fill="#fff" font-weight="900" font-family="sans-serif" text-anchor="middle" dy=".35em">${char}</text></svg>`;
    return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg);
};

// 4. 双轨制获取当前用户身份
function getAuthUser() {
    let user = window.getCookie('username');

    // 如果没有 Cookie，尝试拿 localStorage
    if (!user) {
        try { // 防止浏览器彻底禁用本地存储时报错
            user = localStorage.getItem('fingerprint_token');
            if (user) {
                console.log("检测到无痕凭证，自动恢复会话:", user);
                document.cookie = `username=${user};path=/;max-age=172800`;
            }
        } catch (error) {
            console.warn("本地存储被完全禁用，无法读取无痕凭证");
        }
    }
    return user;
}

// 5. 导出全局状态变量供各页面使用
window.APP_USER = getAuthUser();
window.IS_LOGGED_IN = !!window.APP_USER;