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

// document.getElementById('logout-btn').addEventListener('click', function(event) {
//     event.preventDefault(); // 기본 동작 방지
//
//     // 로그인 유형 확인
//     var loginType = /*[[${session.loginType}]]*/ 'GENERAL'; // Thymeleaf 사용
//
//     if (loginType === 'SNS') {
//         // SNS 로그아웃 경로로 리디렉션
//         window.location.href = '/snslogin/logout';
//     } else {
//         // 일반 회원 로그아웃 경로로 리디렉션
//         window.location.href = '/change/logout';
//     }
// });
//
// $(document).ready(function() {
//     var loginType = localStorage.getItem("loginType");
//     if (!loginType) {
//         console.error("로그인 타입을 확인할 수 없습니다.");
//         return;
//     }
//
//     // 회원정보 수정 링크 설정
//     var updateLink = $("#update-link");
//     var updateUrl;
//     if (loginType === "SNS") {
//         updateUrl = "/snslogin/sns-update";
//     } else if (loginType === "GENERAL") {
//         updateUrl = "/user/update";
//     } else {
//         console.error("알 수 없는 로그인 타입입니다.");
//         return;
//     }
//     updateLink.attr("href", updateUrl);
// });
