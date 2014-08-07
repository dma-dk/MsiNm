SELECT
  m.id
FROM
  message m
WHERE
  m.isLatest = 1
  AND greatest(m.dateTimeUpdated, coalesce(m.dateTimeDeleted, m.dateTimeUpdated)) > ?
  AND greatest(m.dateTimeUpdated, coalesce(m.dateTimeDeleted, m.dateTimeUpdated)) <= ?
ORDER BY greatest(m.dateTimeUpdated, coalesce(m.dateTimeDeleted, m.dateTimeUpdated)) ASC
LIMIT ?
