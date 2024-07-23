document.addEventListener('DOMContentLoaded', function () {
    const emptyTrashBtn = document.getElementById('trash_EmptyTrashBtn');
    const modalPopup = document.querySelector('.trashModal_popup');
    const cancelBtn = document.getElementById('trashModal_cancel');
    const deleteBtn = document.getElementById('trashModal_delete');

    emptyTrashBtn.addEventListener('click', function () {
        modalPopup.style.display = 'flex';
    });

    cancelBtn.addEventListener('click', function () {
        window.location.href = 'trash.html';
    });

    deleteBtn.addEventListener('click', function () {
        window.location.href = 'trashEmp.html';
    });

    window.onclick = function (event) {
        if (event.target == modalPopup) {
            closeModal();
        }
    };
});

function closeModal() {
    const modalPopup = document.querySelector('.trashModal_popup');
    modalPopup.style.display = 'none';
}