function toggleDropdown(event) {
    event.preventDefault(); // 기본 링크 클릭 방지
    const dropdownContent = document.getElementById("mypage-dropdown");
    dropdownContent.classList.toggle("show");
}

// 드롭다운 외부 클릭 시 닫힘
window.addEventListener("click", function(event) {
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
