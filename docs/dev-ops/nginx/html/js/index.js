document.addEventListener('DOMContentLoaded', () => {
    // ==========================================
    // 全局配置与状态变量
    // ==========================================
    // 将此处改为空字符串，依靠浏览器默认拼接域名和相对路径。
    // fetch(`${BASE_URL}/api/v1/...`) 就会变成 fetch(`/api/v1/...`)，完美命中 Nginx 的 location 规则。
    const BASE_URL = 'http://127.0.0.1:8091';
    const ALIPAY_BASE_URL = 'http://127.0.0.1:7860';

    // 动态变量：根据后端接口回传的数据填充
    let globalGoodsId = "9890001";
    let globalActivityId = 100123;

    // 当前交易上下文状态
    let tradeContext = {
        actionType: '', // 'single'(单刷), 'start'(发起), 'join'(加入)
        teamId: '',
        amount: 0
    };

    // ==========================================
    // 1. 无缝无限轮播图逻辑 (保持不变)
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
    // 2. 动态文字头像生成逻辑 (保持不变)
    // ==========================================
    function getAvatarDataURI(name) {
        if (!name) name = "U";
        const char = name.charAt(0).toUpperCase();
        const colors = ['#2b80ff', '#ff9f43', '#00cec9', '#6c5ce7', '#00b894', '#e84393'];
        const color = colors[char.charCodeAt(0) % colors.length] || colors[0];
        const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect width="100" height="100" fill="${color}" rx="25"/><text x="50%" y="50%" font-size="45" fill="#fff" font-weight="900" font-family="sans-serif" text-anchor="middle" dy=".35em">${char}</text></svg>`;
        return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg);
    }

    function getCookie(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for(let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
        }
        return null;
    }

    const username = getCookie('username');
    const isLoggedIn = !!username;

    if (isLoggedIn) {
        document.getElementById('topAvatarImg').src = getAvatarDataURI(username);
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

        // 更新动态全局变量
        if (activityId) globalActivityId = activityId;
        if (goods && goods.goodsId) globalGoodsId = goods.goodsId;

        if (goods && goods.goodsName) {
            document.getElementById('productTitle').innerText = goods.goodsName;
        }

        const deductionPrice = goods?.deductionPrice || 0;
        const userCount = teamStatistic?.allTeamUserCount || 0;
        document.getElementById('discountBanner').innerHTML = `<span>🔥 BUFF加成</span> 直降 ¥${deductionPrice}, 已有 ${userCount} 玩家加入队伍`;

        // 价格分配：单刷 -> originalPrice, 组队 -> payPrice
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
                const avatarSrc = getAvatarDataURI(team.userId);
                const totalSeconds = parseTimeToSeconds(team.validTimeCountdown);

                // 加入队伍金额：payPrice
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
    // 4. 点击动作 -> 呼出统一支付弹窗
    // ==========================================
    const paymentModal = document.getElementById('paymentModal');

    document.body.addEventListener('click', function(e) {
        if (e.target.classList.contains('buy-btn')) {
            const amount = e.target.getAttribute('data-amount');

            if (!isLoggedIn) {
                window.location.href = 'login.html';
                return;
            }

            // 判断点击来源并记录到交易上下文
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

            // 显示支付弹窗及金额
            document.getElementById('paymentAmountText').innerText = `¥${amount}`;
            paymentModal.style.display = 'flex';
        }
    });

    // ==========================================
    // 5. 取消支付 与 直接调用支付宝接口
    // ==========================================
    document.getElementById('btnCancelPay').addEventListener('click', () => {
        paymentModal.style.display = 'none'; // 点击取消支付直接关闭窗口
    });

    // 这里原封不动地保留了你正确的逻辑：直接调用 create_pay_order
    document.getElementById('btnPayNow').addEventListener('click', async () => {
        const marketType = tradeContext.actionType === 'single' ? 0 : 1;

        try {
            // 访问支付宝创建订单接口
            const payRes = await fetch(`${ALIPAY_BASE_URL}/api/v1/alipay/create_pay_order`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    "userId": username,
                    "productId": globalGoodsId, // 动态读取的 goodsId
                    "teamId": tradeContext.teamId,
                    "activityId": globalActivityId, // 动态读取的 activityId
                    "marketType": marketType // 单刷为0，组队为1
                })
            });

            const payData = await payRes.json();

            if (payData.code === "0000") {
                // 获取返回的 HTML 表单并注入自动提交
                const formHtml = payData.data;
                document.body.insertAdjacentHTML('beforeend', formHtml);
                document.forms[document.forms.length - 1].submit();
            } else {
                // 非成功状态
                alert("支付失败请稍后再试");
            }
        } catch (error) {
            console.error("创建支付宝订单异常:", error);
            alert("支付失败请稍后再试");
        }
    });

    // ==========================================
    // 6. 我的订单跳转逻辑
    // ==========================================
    const btnOrders = document.getElementById('btnOrders');
    if (btnOrders) {
        btnOrders.addEventListener('click', () => {
            // 如果需要登录才能看订单，可以在这里加个判断
            if (!isLoggedIn) {
                window.location.href = 'login.html';
                return;
            }
            window.location.href = 'order-list.html';
        });
    }

    // 倒计时工具
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

    // 初始化加载页面数据
    fetchMarketConfig();
});