UPDATE  users
SET resources = resources || $1
WHERE id = $2;