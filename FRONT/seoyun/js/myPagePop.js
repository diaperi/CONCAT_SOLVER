document.addEventListener('DOMContentLoaded', function() {
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

    const overlay = document.getElementById("overlay");
    const popup = document.getElementById("popup");
    const closePopupButton = document.getElementById("close-popup");
    const popupDate = document.getElementById("popup-date");
    const popupVideo = document.getElementById("popup-video");

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
                    cell.addEventListener('click', function() {
                        openPopup(capturedDate, capturedMonth, capturedYear);
                    });
                    date++;
                }
                row.appendChild(cell);
            }
            calendarBody.appendChild(row);
        }
    }

    function openPopup(day, month, year) {
        // 날짜를 올바르게 표시
        const dateStr = `${month + 1}월 ${day}일`;
        popupDate.innerText = dateStr;

        // 비디오 URL을 동적으로 설정합니다.
        // 날짜에 맞는 비디오 URL로 교체해야 합니다.
        const videoURL = `path/to/your/videos/${year}-${month + 1}-${day}.mp4`; // 비디오 URL 수정
        // popupVideo.src = videoURL;

        // 팝업 표시
        overlay.style.display = 'block';
        popup.style.display = 'block';
    }

    function closePopup() {
        overlay.style.display = 'none';
        popup.style.display = 'none';
    }

    prevButton.addEventListener('click', function() {
        currentYear = (currentMonth === 0) ? currentYear - 1 : currentYear;
        currentMonth = (currentMonth === 0) ? 11 : currentMonth - 1;
        showCalendar(currentMonth, currentYear);
    });

    nextButton.addEventListener('click', function() {
        currentYear = (currentMonth === 11) ? currentYear + 1 : currentYear;
        currentMonth = (currentMonth + 1) % 12;
        showCalendar(currentMonth, currentYear);
    });

    closePopupButton.addEventListener('click', closePopup);
    overlay.addEventListener('click', closePopup);

    showCalendar(currentMonth, currentYear);
});
