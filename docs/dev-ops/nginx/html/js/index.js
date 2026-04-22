document.addEventListener('DOMContentLoaded', () => {
    // ==========================================
    // 全局配置与变量
    // ==========================================
    const BASE_URL = 'http://127.0.0.1:8091'; // 全局统一后端地址，方便后期更换
    let currentOutTradeNo = null;             // 用于存放生成的 12 位随机订单号

    // ==========================================
    // 1. 无缝无限轮播图逻辑 (保持不变)
    // ==========================================
    const slidesContainer = document.getElementById('slides');
    const slideElements = document.querySelectorAll('.slide');
    const totalRealSlides = slideElements.length;

    const firstClone = slideElements[0].cloneNode(true);
    const lastClone = slideElements[totalRealSlides - 1].cloneNode(true);

    slidesContainer.appendChild(firstClone);
    slidesContainer.insertBefore(lastClone, slideElements[0]);

    let currentSlide = 1;
    let isTransitioning = false;

    slidesContainer.style.transform = `translateX(-${currentSlide * 100}%)`;

    function changeSlide(direction) {
        if (isTransitioning) return;
        isTransitioning = true;
        currentSlide += direction;

        slidesContainer.style.transition = 'transform 0.4s ease-in-out';
        slidesContainer.style.transform = `translateX(-${currentSlide * 100}%)`;
    }

    slidesContainer.addEventListener('transitionend', () => {
        isTransitioning = false;
        if (currentSlide === 0) {
            slidesContainer.style.transition = 'none';
            currentSlide = totalRealSlides;
            slidesContainer.style.transform = `translateX(-${currentSlide * 100}%)`;
        } else if (currentSlide === totalRealSlides + 1) {
            slidesContainer.style.transition = 'none';
            currentSlide = 1;
            slidesContainer.style.transform = `translateX(-${currentSlide * 100}%)`;
        }
    });

    document.getElementById('btnPrev').addEventListener('click', () => changeSlide(-1));
    document.getElementById('btnNext').addEventListener('click', () => changeSlide(1));
    setInterval(() => changeSlide(1), 3000);

    // ==========================================
    // 2. 动态文字头像生成逻辑
    // ==========================================
    function getAvatarDataURI(name) {
        if (!name) name = "U";
        const char = name.charAt(0).toUpperCase();
        const colors = ['#ff4d4f', '#fca130', '#52c41a', '#1890ff', '#722ed1', '#eb2f96'];
        const color = colors[char.charCodeAt(0) % colors.length] || colors[0];

        const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100">
            <rect width="100" height="100" fill="${color}"/>
            <text x="50%" y="50%" font-size="50" fill="white" font-weight="bold" font-family="sans-serif" text-anchor="middle" dy=".35em">${char}</text>
        </svg>`;
        return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg);
    }

    // ==========================================
    // 3. 业务状态与页面跳转逻辑
    // ==========================================
    function getCookie(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for(let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) {
                return decodeURIComponent(c.substring(nameEQ.length, c.length));
            }
        }
        return null;
    }

    const paymentModal = document.getElementById('paymentModal');
    const isLoggedIn = getCookie('isLoggedIn') === 'true';
    const username = getCookie('username') || "unknown"; // 从 cookie 获取用户名当作 userId

    if (isLoggedIn && username !== "unknown") {
        const topAvatarImg = document.getElementById('topAvatarImg');
        topAvatarImg.src = getAvatarDataURI(username);
        document.getElementById('userAvatar').style.display = 'block';
    }

    const pendingAmount = sessionStorage.getItem('pendingPayAmount');
    if (pendingAmount && isLoggedIn) {
        showPaymentModal(pendingAmount);
        sessionStorage.removeItem('pendingPayAmount');
    }

    function showPaymentModal(amount) {
        document.getElementById('payAmountText').innerText = `支付金额：¥${amount}`;
        paymentModal.style.display = 'flex';
    }

    document.getElementById('btnCancelPay').addEventListener('click', () => {
        paymentModal.style.display = 'none';
        currentOutTradeNo = null; // 取消支付时清理单号
    });

    // ==========================================
    // 4. API 接口对接与动态渲染逻辑
    // ==========================================

    // 生成 12 位随机数字订单号
    function generateOutTradeNo() {
        return String(Math.floor(Math.random() * 1000000000000)).padStart(12, '0');
    }

    // 获取格式化时间 YYYY-MM-DDTHH:mm:ss
    function getFormattedTime() {
        const now = new Date();
        const y = now.getFullYear();
        const m = String(now.getMonth() + 1).padStart(2, '0');
        const d = String(now.getDate()).padStart(2, '0');
        const hh = String(now.getHours()).padStart(2, '0');
        const mm = String(now.getMinutes()).padStart(2, '0');
        const ss = String(now.getSeconds()).padStart(2, '0');
        return `${y}-${m}-${d}T${hh}:${mm}:${ss}`;
    }

    // 解析后端返回的 "15:20:28" 为秒数
    function parseTimeToSeconds(timeStr) {
        if(!timeStr) return 0;
        const parts = timeStr.split(':');
        if(parts.length === 3) {
            return parseInt(parts[0]) * 3600 + parseInt(parts[1]) * 60 + parseInt(parts[2]);
        }
        return 0;
    }

    // 获取页面基础数据
    async function fetchMarketConfig() {
        try {
            const response = await fetch(`${BASE_URL}/api/v1/gbm/index/query_group_buy_market_config`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    "userId": username,
                    "source": "s01",
                    "channel": "c01",
                    "goodsId": "9890001"
                })
            });

            const res = await response.json();
            if (res.code === "0000" && res.data) {
                renderPageData(res.data);
            } else {
                console.error("接口获取失败或状态码不正确:", res.info);
            }
        } catch (error) {
            console.error("请求异常(请检查后端是否开启了允许跨域 CORS):", error);
        }
    }

    function renderPageData(data) {
        const { goods, teamList, teamStatistic } = data;

        // 渲染商品名
        if (goods && goods.goodsName) {
            document.getElementById('productTitle').innerText = goods.goodsName;
        }

        // 渲染优惠横幅 (直降 & 多少人再抢)
        const discountBanner = document.getElementById('discountBanner');
        const deductionPrice = goods?.deductionPrice || 0;
        const userCount = teamStatistic?.allTeamUserCount || 0;
        discountBanner.innerHTML = `<span>大促优惠</span> 直降 ¥${deductionPrice}, ${userCount}人再抢, 参与马上抢到`;

        // 渲染底部购买按钮价格
        const btnBuyAlone = document.getElementById('btnBuyAlone');
        const btnBuyGroup = document.getElementById('btnBuyGroup');
        if(goods) {
            btnBuyAlone.innerText = `单独购买(¥${goods.originalPrice})`;
            btnBuyAlone.setAttribute('data-amount', goods.originalPrice);

            btnBuyGroup.innerText = `开团购买(¥${goods.payPrice})`;
            btnBuyGroup.setAttribute('data-amount', goods.payPrice);
        }

        // 渲染拼团列表
        const groupBuyList = document.getElementById('groupBuyList');
        groupBuyList.innerHTML = ''; // 清空占位

        if (!teamList || teamList.length === 0) {
            groupBuyList.innerHTML = `
                <div style="text-align:center; padding: 30px 15px; color: #999; font-size: 14px;">
                    小伙伴，赶紧去开团吧，做村里最靓的仔。
                </div>`;
        } else {
            teamList.forEach(team => {
                const remainCount = team.targetCount - team.lockCount;
                const avatarSrc = getAvatarDataURI(team.userId);
                const totalSeconds = parseTimeToSeconds(team.validTimeCountdown);

                const itemHtml = `
                    <div class="group-item">
                        <div class="user-info">
                            <img class="avatar" src="${avatarSrc}" alt="${team.userId}">
                            <div class="user-details">
                                <h4>${team.userId}</h4>
                                <p>组队仅剩${remainCount}人, 拼单即将结束 <span class="time timer-display" data-time="${totalSeconds}">${team.validTimeCountdown}</span></p>
                            </div>
                        </div>
                        <button class="join-btn buy-btn" data-amount="${goods?.payPrice || 0}" data-team-id="${team.teamId}">参与拼团</button>
                    </div>
                `;
                groupBuyList.insertAdjacentHTML('beforeend', itemHtml);
            });
        }
    }

    // ==========================================
    // 5. 点击购买（锁单逻辑）与 支付结算逻辑
    // ==========================================

    // 点击购买/拼团 按钮触发锁单
    document.body.addEventListener('click', async function(e) {
        if (e.target.classList.contains('buy-btn')) {
            const amount = e.target.getAttribute('data-amount');

            if (!isLoggedIn) {
                sessionStorage.setItem('pendingPayAmount', amount);
                window.location.href = 'login.html';
                return;
            }

            // 判断是否是单独购买。如果是，跳过拼团锁单逻辑，直接弹窗
            const isBuyAlone = e.target.id === 'btnBuyAlone';

            if (!isBuyAlone) {
                // 读取 teamId。如果是点击“开团购买” (btnBuyGroup)，teamId 应该为空；若是列表中的“参与拼团”，则有值。
                const targetTeamId = e.target.getAttribute('data-team-id') || "";
                currentOutTradeNo = generateOutTradeNo();

                try {
                    const lockRes = await fetch(`${BASE_URL}/api/v1/gbm/trade/lock_market_pay_order`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            "userId": username,
                            "teamId": targetTeamId,
                            "goodsId": "9890001",
                            "activityId": 100123,
                            "source": "s01",
                            "channel": "c01",
                            "outTradeNo": currentOutTradeNo,
                            "notifyUrl": `${BASE_URL}/api/tewst/group_buy_notify`
                        })
                    });
                    const lockData = await lockRes.json();

                    if (lockData.code !== "0000") {
                        alert(`锁单失败: ${lockData.info || '未知错误'}`);
                        return; // 失败则终止弹窗
                    }
                } catch (error) {
                    console.error("锁单接口请求异常:", error);
                    alert("网络异常，锁单失败");
                    return;
                }
            }
            // 锁单成功（或单买），展示支付二维码
            showPaymentModal(amount);
        }
    });

    // 支付完成点击（结算逻辑）
    document.getElementById('btnConfirmPay').addEventListener('click', async () => {
        // 单买没有 currentOutTradeNo，可以直接过掉
        if (!currentOutTradeNo) {
            alert('支付成功！（单买，未走拼团结算逻辑）');
            paymentModal.style.display = 'none';
            return;
        }

        try {
            const settleRes = await fetch(`${BASE_URL}/api/v1/gbm/trade/settlement_market_pay_order`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    "source": "s01",
                    "channel": "c01",
                    "userId": username,
                    "outTradeNo": currentOutTradeNo,
                    "outTradeTime": getFormattedTime()
                })
            });

            const settleData = await settleRes.json();
            if (settleData.code === "0000") {
                alert('支付并结算完成！');
                paymentModal.style.display = 'none';
                currentOutTradeNo = null; // 清除使用完的订单号
                fetchMarketConfig(); // 刷新页面数据（重新获取拼团列表）
            } else {
                alert(`结算失败: ${settleData.info || '未知错误'}`);
            }
        } catch (error) {
            console.error("结算接口请求异常:", error);
            alert("网络异常，结算失败");
        }
    });

    // ==========================================
    // 6. 动态倒计时逻辑
    // ==========================================
    setInterval(() => {
        document.querySelectorAll('.timer-display').forEach(el => {
            let timeRemaining = parseInt(el.getAttribute('data-time') || 0);
            if(timeRemaining > 0) {
                timeRemaining--;
                el.setAttribute('data-time', timeRemaining);

                let h = Math.floor(timeRemaining / 3600).toString();
                let m = Math.floor((timeRemaining % 3600) / 60).toString().padStart(2, '0');
                let s = (timeRemaining % 60).toString().padStart(2, '0');

                el.innerText = `${h}:${m}:${s}`;
            }
        });
    }, 1000);

    // 页面加载完成后触发获取商品及拼团数据
    fetchMarketConfig();
});