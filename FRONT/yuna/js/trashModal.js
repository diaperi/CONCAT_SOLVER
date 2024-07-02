document.addEventListener('DOMContentLoaded', function() {
    var trashModal_cancel = document.getElementById('trashModal_cancel');
    var trashModal_delete = document.getElementById('trashModal_delete');
    
    if (trashModal_cancel) {
        trashModal_cancel.addEventListener('click', function() {
            window.location.href = 'trash.html';
        });
    }

    if (trashModal_delete) {
        trashModal_delete.addEventListener('click', function() {
            window.location.href = 'trashEmp.html';
        });
    }
});