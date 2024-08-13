document.addEventListener('DOMContentLoaded', function () {
    const downBarButton = document.querySelector('.myPageMain_downBar');
    const upBarButton = document.querySelector('.myPageMain_UpBar');
    const targetSection2 = document.querySelector('.header_navbar');

    downBarButton.addEventListener('click', function () {
        window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});
    });

    upBarButton.addEventListener('click', function () {
        targetSection2.scrollIntoView({behavior: 'smooth'});
    });

    // 삭제 후 새로운 목록을 Ajax로 불러오는 로직
    function updateList() {
        $.ajax({
            url: '/mypagemain/updateList',
            method: 'GET',
            success: function(data) {
                // 데이터를 받아와서 목록을 갱신
                $('#fileList').html(data);
            },
            error: function(xhr, status, error) {
                console.error('목록 업데이트 중 오류 발생:', error);
                alert('목록 업데이트에 실패했습니다.');
            }
        });
    }
    // 페이지 로드 후 초기 목록 로드
    updateList();
});
