<?php
header('Content-Type: application/json');

$existingEmails = array("example@example.com", "test@test.com");
$email = json_decode(file_get_contents('php://input'), true)['email'];

$response = array('exists' => in_array($email, $existingEmails));
echo json_encode($response);
?>
