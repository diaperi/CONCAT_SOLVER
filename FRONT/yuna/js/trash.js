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
        window.location.href = 'trashEmp.html';
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
