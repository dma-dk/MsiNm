SELECT
  m.id
FROM
  message m
WHERE
  m.isLatest = 1
  AND m.dateTimeUpdated > ?
  AND m.dateTimeUpdated <= ?
ORDER BY m.dateTimeUpdated ASC
LIMIT ?
