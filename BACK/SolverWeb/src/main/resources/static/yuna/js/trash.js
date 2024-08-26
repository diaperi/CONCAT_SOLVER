document.addEventListener('DOMContentLoaded', function () {
    const bucketBaseUrl = 'https://diaperiwinklebucket2.s3.ap-northeast-2.amazonaws.com/';

    const emptyTrashBtn = document.getElementById('trash_EmptyTrashBtn');
    const modalPopup = document.querySelector('.trashModal_popup');
    const cancelBtn = document.getElementById('trashModal_cancel');
    const deleteBtn = document.getElementById('trashModal_delete');
    const upBar = document.querySelector('.trash_upBar');
    const targetSection = document.querySelector('main');

    emptyTrashBtn.addEventListener('click', function () {
        modalPopup.style.display = 'flex';
    });

    cancelBtn.addEventListener('click', function () {
        closeModal();
    });

    deleteBtn.addEventListener('click', function () {
        fetch('/myPage/trash/videos/empty', {
            method: 'DELETE'
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.location.href = '/myPage/trashEmp'; // Redirect to trashEmp.html
                } else {
                    alert('휴지통 비우기에 실패했습니다: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Fetch 실패:', error);
                alert('휴지통 비우기에 실패했습니다.');
            });
    });

    window.onclick = function (event) {
        if (event.target === modalPopup) {
            closeModal();
        } else if (!event.target.closest('.trash_moreOptions') && !event.target.closest('.trash_moreOptionsMenu')) {
            document.querySelectorAll('.trash_moreOptionsMenu').forEach(menu => {
                menu.classList.remove('show');
            });
        }
    };

    upBar.addEventListener('click', function () {
        targetSection.scrollIntoView({ behavior: 'smooth' });
    });

    // 비디오 로드
    fetch('/myPage/trash/videos')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const sections = {
                    today: document.querySelector('.today'),
                    thisWeek: document.querySelector('.this-week'),
                    last1Week: document.querySelector('.last-1week'),
                    last2Week: document.querySelector('.last-2week'),
                    last3Week: document.querySelector('.last-3week'),
                    last4Week: document.querySelector('.last-4week')
                };

                let allSectionsEmpty = true;

                Object.keys(sections).forEach(section => {
                    const container = sections[section].querySelector('.trash_itemContainer');
                    const videoKeys = data.videos[section] || [];

                    if (videoKeys.length === 0) {
                        sections[section].querySelector('.trash_sectionTitle').style.display = 'none';
                    } else {
                        videoKeys.forEach(videoKey => {
                            const itemBox = document.createElement('div');
                            itemBox.className = 'trash_itemBox';
                            itemBox.innerHTML = `
                                <div class="trash_itemHead">
                                    <img src="/yuna/img/trash_videoIcon.png" alt="비디오-아이콘">
                                    <p>비디오 제목</p>
                                    <div class="trash_itemClick">
                                        <button class="trash_moreOptions">
                                            <img src="/yuna/img/trash_3dotsIcon.png" alt="점3개">
                                        </button>
                                        <div class="trash_moreOptionsMenu">
                                            <button class="trash_recoverBtn">
                                                <span class="recoveryText">복구하기</span>
                                            </button>
                                        </div>
                                    </div>
                                </div>
                                <div class="trash_itemBody">
                                    <img class="trash_img" src="${bucketBaseUrl}${videoKey}" alt="쓰레기">
                                </div>
                            `;
                            container.appendChild(itemBox);
                        });
                        allSectionsEmpty = false;
                    }
                });

                if (allSectionsEmpty) {
                    window.location.href = '/myPage/trashEmp';
                }

                // 복구 버튼 이벤트 리스너
                addRecoverButtonListeners();
            } else {
                console.error('비디오 목록을 가져오는 데 실패했습니다:', data.error);
            }
        })
        .catch(error => {
            console.error('Fetch 실패:', error);
        });

    function closeModal() {
        modalPopup.style.display = 'none';
    }

    function addRecoverButtonListeners() {
        const moreOptionsBtns = document.querySelectorAll('.trash_moreOptions');

        moreOptionsBtns.forEach(btn => {
            btn.addEventListener('click', function (event) {
                event.stopPropagation();
                const menu = btn.nextElementSibling;
                menu.classList.toggle('show');
            });
        });

        const recoverBtns = document.querySelectorAll('.trash_recoverBtn');

        recoverBtns.forEach(btn => {
            btn.addEventListener('click', function () {
                const videoKey = btn.closest('.trash_itemBox').querySelector('.trash_img').src.split('/').pop();
                recoverVideo(videoKey);
            });
        });
    }

    function recoverVideo(videoKey) {
        fetch('/myPage/trash/videos/recover', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                videoKey: videoKey
            })
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('복구 성공');
                    location.reload(); // 페이지 새로고침하여 업데이트
                } else {
                    alert('비디오 복구 실패: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Fetch 실패:', error);
                alert('복구 실패.');
            });
    }
});
