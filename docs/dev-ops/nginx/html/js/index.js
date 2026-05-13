// index.js
document.addEventListener('DOMContentLoaded', () => {
    // 从全局配置读取常量和用户状态
    const { BASE_URL, ALIPAY_BASE_URL } = window.APP_CONFIG;
    const username = window.APP_USER;
    const isLoggedIn = window.IS_LOGGED_IN;

    // 动态变量：根据后端接口回传的数据填充
    let globalGoodsId = "9890001";
    let globalActivityId = 100123;

    let tradeContext = {
        actionType: '',
        teamId: '',
        amount: 0
    };

    // ==========================================
    // 1. 无缝无限轮播图逻辑
    // ==========================================
    const slidesContainer = document.getElementById('slides');
    const slideElements = document.querySelectorAll('.slide');
    const totalRealSlides = slideElements.length;

    if (totalRealSlides > 0) {
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
    }

    // ==========================================
    // 2. 界面初始化 (渲染头像)
    // ==========================================
    if (isLoggedIn) {
        document.getElementById('topAvatarImg').src = window.getAvatarDataURI(username);
        document.getElementById('userAvatar').style.display = 'block';
    }

    // ==========================================
    // 3. API 接口对接与动态渲染逻辑
    // ==========================================
    function parseTimeToSeconds(timeStr) {
        if(!timeStr) return 0;
        const parts = timeStr.split(':');
        if(parts.length === 3) return parseInt(parts[0]) * 3600 + parseInt(parts[1]) * 60 + parseInt(parts[2]);
        return 0;
    }

    async function fetchMarketConfig() {
        try {
            const response = await fetch(`${BASE_URL}/api/v1/gbm/index/query_group_buy_market_config`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    "userId": username || "unknown",
                    "source": "s01",
                    "channel": "c01",
                    "goodsId": "9890001"
                })
            });

            const res = await response.json();
            if (res.code === "0000" && res.data) {
                renderPageData(res.data);
            } else {
                console.error("接口获取失败:", res.info);
            }
        } catch (error) {
            console.error("请求异常:", error);
        }
    }

    function renderPageData(data) {
        const { goods, teamList, teamStatistic, activityId } = data;

        if (activityId) globalActivityId = activityId;
        if (goods && goods.goodsId) globalGoodsId = goods.goodsId;

        if (goods && goods.goodsName) {
            document.getElementById('productTitle').innerText = goods.goodsName;
        }

        const deductionPrice = goods?.deductionPrice || 0;
        const userCount = teamStatistic?.allTeamUserCount || 0;
        document.getElementById('discountBanner').innerHTML = `<span>🔥 BUFF加成</span> 直降 ¥${deductionPrice}, 已有 ${userCount} 玩家加入队伍`;

        if(goods) {
            const btnAlone = document.getElementById('btnBuyAlone');
            btnAlone.innerText = `单刷 (¥${goods.originalPrice})`;
            btnAlone.setAttribute('data-amount', goods.originalPrice);

            const btnGroup = document.getElementById('btnBuyGroup');
            btnGroup.innerText = `发起组队 (¥${goods.payPrice})`;
            btnGroup.setAttribute('data-amount', goods.payPrice);
        }

        const groupBuyList = document.getElementById('groupBuyList');
        groupBuyList.innerHTML = '';

        if (!teamList || teamList.length === 0) {
            groupBuyList.innerHTML = `<div style="text-align:center; padding: 40px 15px; color: var(--text-light); font-size: 14px; font-weight:bold;">🎮 暂无开放的队伍，快来做房主吧！</div>`;
        } else {
            teamList.forEach(team => {
                const remainCount = team.targetCount - team.lockCount;
                const avatarSrc = window.getAvatarDataURI(team.userId);
                const totalSeconds = parseTimeToSeconds(team.validTimeCountdown);

                const itemHtml = `
                <div class="group-item">
                    <div class="user-info">
                        <img class="avatar" src="${avatarSrc}" alt="${team.userId}">
                        <div class="user-details">
                            <h4>ID: ${team.userId}</h4>
                            <p>队伍缺 ${remainCount} 人 <i class="live-dot"></i> <span class="time timer-display" data-time="${totalSeconds}">${team.validTimeCountdown}</span></p>
                        </div>
                    </div>
                    <button class="join-btn buy-btn" data-amount="${goods?.payPrice || 0}" data-team-id="${team.teamId}">加入队伍</button>
                </div>`;
                groupBuyList.insertAdjacentHTML('beforeend', itemHtml);
            });
        }
    }

    // ==========================================
    // 4. 支付逻辑绑定
    // ==========================================
    const paymentModal = document.getElementById('paymentModal');

    document.body.addEventListener('click', function(e) {
        if (e.target.classList.contains('buy-btn')) {
            const amount = e.target.getAttribute('data-amount');

            if (!isLoggedIn) {
                window.location.href = 'login.html';
                return;
            }

            tradeContext.amount = amount;
            if (e.target.id === 'btnBuyAlone') {
                tradeContext.actionType = 'single';
                tradeContext.teamId = '';
            } else if (e.target.id === 'btnBuyGroup') {
                tradeContext.actionType = 'start';
                tradeContext.teamId = '';
            } else {
                tradeContext.actionType = 'join';
                tradeContext.teamId = e.target.getAttribute('data-team-id') || "";
            }

            document.getElementById('paymentAmountText').innerText = `¥${amount}`;
            paymentModal.style.display = 'flex';
        }
    });

    document.getElementById('btnCancelPay').addEventListener('click', () => {
        paymentModal.style.display = 'none';
    });

    document.getElementById('btnPayNow').addEventListener('click', async () => {
        const marketType = tradeContext.actionType === 'single' ? 0 : 1;

        try {
            const payRes = await fetch(`${ALIPAY_BASE_URL}/api/v1/alipay/create_pay_order`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    "userId": username,
                    "productId": globalGoodsId,
                    "teamId": tradeContext.teamId,
                    "activityId": globalActivityId,
                    "marketType": marketType
                })
            });

            const payData = await payRes.json();

            if (payData.code === "0000") {
                const formHtml = payData.data;
                document.body.insertAdjacentHTML('beforeend', formHtml);
                document.forms[document.forms.length - 1].submit();
            } else {
                alert("支付失败请稍后再试");
            }
        } catch (error) {
            console.error("创建支付宝订单异常:", error);
            alert("支付失败请稍后再试");
        }
    });

    // ==========================================
    // 5. 导航与工具
    // ==========================================
    const btnOrders = document.getElementById('btnOrders');
    if (btnOrders) {
        btnOrders.addEventListener('click', () => {
            if (!isLoggedIn) {
                window.location.href = 'login.html';
                return;
            }
            window.location.href = 'order-list.html';
        });
    }

    setInterval(() => {
        document.querySelectorAll('.timer-display').forEach(el => {
            let t = parseInt(el.getAttribute('data-time') || 0);
            if(t > 0) {
                t--; el.setAttribute('data-time', t);
                let h = Math.floor(t / 3600).toString();
                let m = Math.floor((t % 3600) / 60).toString().padStart(2, '0');
                let s = (t % 60).toString().padStart(2, '0');
                el.innerText = `${h}:${m}:${s}`;
            }
        });
    }, 1000);

    fetchMarketConfig();
});