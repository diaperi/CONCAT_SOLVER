document.addEventListener('DOMContentLoaded', () => {
    const mypageDropdown = document.getElementById('mypage-dropdown');

    // 드롭다운 외부 클릭 시 닫기
    window.addEventListener('click', (e) => {
        const mypageBtn = document.getElementById('mypage-btn');
        if (!mypageBtn.contains(e.target) && !mypageDropdown.contains(e.target)) {
            mypageDropdown.style.display = 'none';
        }
    });
});