document.addEventListener('DOMContentLoaded', function () {
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
        fetch('/myPage/cleanupTrash', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    // 삭제 후 페이지 새로고침
                    window.location.reload();
                } else {
                    console.error('Failed to delete trash');
                }
            })
            .catch(error => console.error('Error:', error));

        closeModal();
    });

    window.onclick = function (event) {
        if (event.target == modalPopup) {
            closeModal();
        }
    };

    upBar.addEventListener('click', function () {
        targetSection.scrollIntoView({ behavior: 'smooth' });
    });
});

function closeModal() {
    const modalPopup = document.querySelector('.trashModal_popup');
    modalPopup.style.display = 'none';
}
