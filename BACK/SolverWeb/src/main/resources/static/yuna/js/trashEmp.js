document.addEventListener('DOMContentLoaded', function () {
    const emptyTrashBtn = document.getElementById('trash_EmptyTrashBtn');
    const modalPopup = document.querySelector('.trashModal_popup');
    const cancelBtn = document.getElementById('trashModal_cancel');

    emptyTrashBtn.addEventListener('click', function () {
        modalPopup.style.display = 'flex';
    });

    cancelBtn.addEventListener('click', function () {
        closeModal();
    });

    modalContent.addEventListener('click', function (event) {
        event.stopPropagation();
    });

    function closeModal() {
        modalPopup.style.display = 'none';
    }
});