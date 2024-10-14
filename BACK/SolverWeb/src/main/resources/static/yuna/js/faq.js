let currentPage = 1;
const itemsPerPage = 7;

// 검색어를 저장
let searchQuery = '';

// FAQ 데이터를 저장
let faqs = [];

// FAQ를 표시
function displayFaqs() {
    const faqContainer = document.getElementById('faqContainer');
    faqContainer.innerHTML = '';

    // 검색어가 있을 경우, 필터링된 FAQ 목록 생성
    const filteredFaqs = faqs.filter(faq =>
        faq.question.includes(searchQuery) || searchQuery === ''
    );

    const start = (currentPage - 1) * itemsPerPage;
    const end = Math.min(start + itemsPerPage, filteredFaqs.length);
    const paginatedFaqs = filteredFaqs.slice(start, end);

    paginatedFaqs.forEach((faq, index) => {
        const faqContent = document.createElement('div');
        faqContent.classList.add('faqContent');

        const filteredIndex = start + index;

        faqContent.setAttribute('onclick', `toggleFaq(${filteredIndex})`);
        faqContent.innerHTML = `
            <p>Q. ${faq.question} 
                <span id="arrow_${filteredIndex}" class="arrow">▲</span>
            </p>
            <div id="answer_${filteredIndex}" class="faqAnswer" style="display: none;">
                <p>A. ${faq.answer}</p>
            </div>
            <hr>
        `;
        faqContainer.appendChild(faqContent);
    });

    updatePagination(filteredFaqs.length);
}

// 페이지 업데이트
function updatePagination(filteredCount) {
    const pagination = document.getElementById('pagination');
    pagination.innerHTML = '';

    const pageCount = Math.ceil(filteredCount / itemsPerPage);

    for (let i = 1; i <= pageCount; i++) {
        const pageLink = document.createElement('a');
        pageLink.href = '#';
        pageLink.innerText = i;

        if (i === currentPage) {
            pageLink.classList.add('active');
        }

        // 클릭 시 페이지 변경
        pageLink.onclick = (e) => {
            e.preventDefault();
            currentPage = i; // 현재 페이지
            displayFaqs(); // FAQ 목록 다시 표시
        };

        pagination.appendChild(pageLink);
        pagination.appendChild(document.createTextNode(' '));
    }
}

// FAQ 항목 토글
function toggleFaq(localIndex) {
    const allAnswers = document.querySelectorAll('.faqAnswer');
    const allArrows = document.querySelectorAll('.arrow');

    const currentAnswer = allAnswers[localIndex];
    const currentArrow = allArrows[localIndex];

    let isCurrentlyOpen = currentAnswer.style.display === 'block';

    allAnswers.forEach((answer, i) => {
        answer.style.display = 'none';
        allArrows[i].innerText = '▲';
    });

    if (!isCurrentlyOpen) {
        currentAnswer.style.display = 'block';
        currentArrow.innerText = '▼';
    }
}

// JSON 파일에서 불러오기
function loadFaqs() {
    fetch('/yuna/data/faqs.json')
        .then(response => response.json())
        .then(data => {
            faqs = data;
            displayFaqs();
        })
        .catch(error => console.error('Error fetching the FAQs:', error));
}

// 검색 기능
document.getElementById('faqSearchInput').addEventListener('input', (e) => {
    searchQuery = e.target.value;
    currentPage = 1;
    displayFaqs();
});

// FAQ 불러오기
loadFaqs();