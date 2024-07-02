document.addEventListener('DOMContentLoaded', function() {
    var trash_EmptyTrashBtn = document.getElementById('trash_EmptyTrashBtn');
    
    if (trash_EmptyTrashBtn) {
        trash_EmptyTrashBtn.addEventListener('click', function() {
            window.location.href = 'trashModal.html';
        });
    }
});