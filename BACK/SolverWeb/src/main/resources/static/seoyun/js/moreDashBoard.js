document.addEventListener('DOMContentLoaded', function () {
    const monthNames = ["January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"];

    let today = new Date();
    let currentMonth = today.getMonth();
    let currentYear = today.getFullYear();
    let currentDate = today.getDate(); // Get current date

    const monthYear = document.getElementById("month-year");
    const calendarBody = document.querySelector("#calendar-body tbody");

    const prevButton = document.getElementById("prev");
    const nextButton = document.getElementById("next");


    function showCalendar(month, year) {
        let firstDay = (new Date(year, month)).getDay();
        let daysInMonth = new Date(year, month + 1, 0).getDate();

        calendarBody.innerHTML = "";
        monthYear.innerHTML = `${monthNames[month]} ${year}`;

        let date = 1;

        for (let i = 0; i < 6; i++) {
            let row = document.createElement("tr");

            for (let j = 0; j < 7; j++) {
                let cell = document.createElement("td");
                if (i === 0 && j < firstDay) {
                    cell.textContent = "";
                } else if (date > daysInMonth) {
                    cell.textContent = "";
                } else {
                    cell.textContent = date;
                    cell.dataset.date = `${year}-${month + 1}-${date}`; // 날짜 데이터 설정
                    if (j === 0) {
                        cell.classList.add("sunday");
                    }
                    if (date === currentDate && month === today.getMonth() && year === today.getFullYear()) {
                        cell.classList.add("today"); // Highlight today's date
                    }
                    // Capture the current values of date, month, and year
                    const capturedDate = date;
                    const capturedMonth = month;
                    const capturedYear = year;
                    cell.addEventListener('click', function () {
                        openPopup(capturedDate, capturedMonth, capturedYear);
                    });
                    date++;
                }
                row.appendChild(cell);
            }
            calendarBody.appendChild(row);
        }
    }


    prevButton.addEventListener('click', function () {
        currentYear = (currentMonth === 0) ? currentYear - 1 : currentYear;
        currentMonth = (currentMonth === 0) ? 11 : currentMonth - 1;
        showCalendar(currentMonth, currentYear);
    });

    nextButton.addEventListener('click', function () {
        currentYear = (currentMonth === 11) ? currentYear + 1 : currentYear;
        currentMonth = (currentMonth + 1) % 12;
        showCalendar(currentMonth, currentYear);
    });


    showCalendar(currentMonth, currentYear);
});


const scrollArrow = document.querySelector('.scroll-arrow');
const arrowIcon = scrollArrow.querySelector('i');
const topElement = document.querySelector('.moreDashBoard_div1');
const downElement = document.querySelector('.moreDashBoard_down');

scrollArrow.addEventListener('click', function () {
    if (arrowIcon.classList.contains('fa-angles-down')) {
        // 아래로 스크롤
        downElement.scrollIntoView({behavior: 'smooth'});
    } else {
        // 위로 스크롤
        topElement.scrollIntoView({behavior: 'smooth'});
    }
});

window.addEventListener('scroll', function () {
    // 현재 스크롤 위치가 화면의 중간을 지났는지 확인
    if (window.scrollY > window.innerHeight / 2) {
        // 버튼을 위로 가는 화살표로 변경
        arrowIcon.classList.remove('fa-angles-down');
        arrowIcon.classList.add('fa-angles-up');
    } else {
        // 버튼을 아래로 가는 화살표로 변경
        arrowIcon.classList.remove('fa-angles-up');
        arrowIcon.classList.add('fa-angles-down');
    }
});
document.addEventListener('DOMContentLoaded', function () {
    const element = document.querySelector('.moreDashBoard_top_left_top');
    if (element) {
        // 사용자가 스크롤할 때 애니메이션이 동작하도록 하기 위해 Intersection Observer를 사용할 수도 있습니다.
        // element.classList.add('fade-in-up');

        // 페이지 로드 시 애니메이션을 적용하려면 아래 코드 사용
        setTimeout(() => {
            element.classList.add('fade-in-up');
        }, 100); // 약간의 지연을 줘서 애니메이션이 부드럽게 시작되도록 설정
    }
});


let currentPage = 0;

// 화살표 클릭 시 비동기로 데이터 요청
document.getElementById('left-arrow').addEventListener('click', function () {
    if (currentPage > 0) {
        currentPage--;
        loadDateList(currentPage);
    }
});

document.getElementById('right-arrow').addEventListener('click', function () {
    currentPage++;
    loadDateList(currentPage);
});


// 지피티 제목
function loadDateList(page) {
    fetch(`/moreDashBoard/dateList?page=${page}`)
        .then(response => response.json())
        .then(data => {
            // 날짜 리스트 컨테이너 초기화
            const dateListContainer = document.getElementById('date-list-container');
            dateListContainer.innerHTML = '';

            // 새로운 제목 리스트 추가
            data.forEach(item => {
                const div = document.createElement('div');
                div.classList.add('moreDashBoard_top_right5_dateList1');
                div.innerHTML = `<span>📌</span> <span>${item.date}</span> <span>${item.title}</span>`;
                dateListContainer.appendChild(div);
            });
        })
        .catch(error => console.error('Error fetching data:', error));
}

// 초기 페이지 로드
loadDateList(currentPage);
