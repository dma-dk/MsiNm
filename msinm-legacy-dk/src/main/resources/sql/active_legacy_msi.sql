SELECT
  m.id
FROM
  message m
WHERE
  m.validFrom <= ?
  AND m.dateTimeUpdated > ?
  AND (m.validTo IS NULL OR m.validTo > now())
  AND m.isLatest=1
  AND m.dateTimeDeleted IS NULL
  AND draft=0
ORDER BY m.dateTimeUpdated ASC
LIMIT ?
