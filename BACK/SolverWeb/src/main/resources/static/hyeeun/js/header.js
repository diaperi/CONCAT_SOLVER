// document.addEventListener('DOMContentLoaded', () => {
//     const mypageDropdown = document.getElementById('mypage-dropdown');
//
//     // 드롭다운 외부 클릭 시 닫기
//     window.addEventListener('click', (e) => {
//         const mypageBtn = document.getElementById('mypage-btn');
//         if (!mypageBtn.contains(e.target) && !mypageDropdown.contains(e.target)) {
//             mypageDropdown.style.display = 'none';
//         }
//     });
// });
//
// document.addEventListener('DOMContentLoaded', () => {
//     const mypageDropdown = document.getElementById('train-dropdown');
//
//     // 드롭다운 외부 클릭 시 닫기
//     window.addEventListener('click', (e) => {
//         const mypageBtn = document.getElementById('train-btn');
//         if (!mypageBtn.contains(e.target) && !mypageDropdown.contains(e.target)) {
//             mypageDropdown.style.display = 'none';
//         }
//     });
// });
//
// // document.getElementById('logout-btn').addEventListener('click', function(event) {
// //     event.preventDefault(); // 기본 동작 방지
// //
// //     // 로그인 유형 확인
// //     var loginType = /*[[${session.loginType}]]*/ 'GENERAL'; // Thymeleaf 사용
// //
// //     if (loginType === 'SNS') {
// //         // SNS 로그아웃 경로로 리디렉션
// //         window.location.href = '/snslogin/logout';
// //     } else {
// //         // 일반 회원 로그아웃 경로로 리디렉션
// //         window.location.href = '/change/logout';
// //     }
// // });
// //
// // $(document).ready(function() {
// //     var loginType = localStorage.getItem("loginType");
// //     if (!loginType) {
// //         console.error("로그인 타입을 확인할 수 없습니다.");
// //         return;
// //     }
// //
// //     // 회원정보 수정 링크 설정
// //     var updateLink = $("#update-link");
// //     var updateUrl;
// //     if (loginType === "SNS") {
// //         updateUrl = "/snslogin/sns-update";
// //     } else if (loginType === "GENERAL") {
// //         updateUrl = "/user/update";
// //     } else {
// //         console.error("알 수 없는 로그인 타입입니다.");
// //         return;
// //     }
// //     updateLink.attr("href", updateUrl);
// // });
// // /hyeeun/js/header.js 파일에 추가
//
// document.addEventListener("DOMContentLoaded", function () {
//     // 로딩 스피너를 표시하는 함수
//     function showLoadingSpinner() {
//         const spinner = document.getElementById('loading-spinner');
//         if (spinner) {
//             spinner.style.display = 'flex';
//         }
//     }
//
//     // 마이페이지 버튼 클릭 시 로딩 스피너 표시
//     const myPageBtn = document.getElementById('mypage-btn');
//     if (myPageBtn) {
//         myPageBtn.addEventListener('click', function (event) {
//             showLoadingSpinner();
//         });
//     }
// });
function toggleDropdown(event) {
    event.preventDefault(); // 기본 링크 클릭 방지
    const dropdownContent = document.getElementById("mypage-dropdown");
    dropdownContent.classList.toggle("show");
}

// 드롭다운 외부 클릭 시 닫힘
window.addEventListener("click", function (event) {
    const dropdownContent = document.getElementById("mypage-dropdown");
    const mypageBtn = document.getElementById("mypage-btn");

    if (!mypageBtn.contains(event.target)) {
        dropdownContent.classList.remove("show");
    }
});


// **********************************************************

const headerLogo = document.querySelector('.header_logo');
const headerMenu = document.querySelector('.header_menu');
let hideMenuTimeout; // 타이머를 저장할 변수

// header_logo에 마우스 오버 시 메뉴 표시 및 타이머 설정
headerLogo.addEventListener('mouseenter', () => {
    clearTimeout(hideMenuTimeout); // 기존 타이머가 있으면 제거
    headerMenu.classList.add('show'); // 메뉴 표시

    // 3초 후에 메뉴를 숨기는 타이머 설정
    hideMenuTimeout = setTimeout(() => {
        headerLogo.style.opacity = '1'; // 로고 이미지 다시 표시
        headerMenu.classList.remove('show'); // 메뉴 숨기기
    }, 2000); // 3000ms = 3초
});

// header_menu에 마우스 오버 시 타이머 초기화하여 유지
headerMenu.addEventListener('mouseenter', () => {
    clearTimeout(hideMenuTimeout); // 타이머 제거하여 메뉴 유지
});

// header_menu에서 마우스를 떼면 3초 후에 메뉴 숨김
headerMenu.addEventListener('mouseleave', () => {
    hideMenuTimeout = setTimeout(() => {
        headerLogo.style.opacity = '1';
        headerMenu.classList.remove('show');
    }, 3000); // 3초 후 메뉴 숨기기
});


document.querySelectorAll('.accordion-button').forEach(button => {
    button.addEventListener('click', () => {
        const content = button.nextElementSibling;
        content.style.display = content.style.display === 'block' ? 'none' : 'block';
    });
});
