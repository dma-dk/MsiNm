  SELECT
    msg.id              AS id,
    msg.messageId       AS messageId,
    msg.draft           AS statusDraft,
    msg.navtexNo        AS navtexNo,
    msg.navwarning      AS keySubject,
    msg.enctext         AS amplifyingRemarks,
    msg.validFrom       AS issueDate,
    msg.validTo         AS cancellationDate,
    msg.datetimeCreated AS created,
    msg.dateTimeUpdated AS updated,
    msg.dateTimeDeleted AS deleted,
    msg.comments        AS amplifyingRemarks2,

    prio.priority       AS priority,

    cls.class           AS messageType,

    cat.english         AS specificCategory,
    subcat.english      AS specificLocation,

    a.area_english      AS generalArea,
    loc.subarea         AS locality,
    loctp.type          AS locationType,

    pt.ptnNo            AS pointIndex,
    pt.latitude         AS pointLatitude,
    pt.longitude        AS pointLongitude,
    pt.radius           AS pointRadius
  FROM
    message msg
    LEFT JOIN priority prio ON msg.priorityId = prio.id
    LEFT JOIN msg_class cls ON msg.msgClassId = cls.id
    LEFT JOIN msg_category cat ON msg.msgCategoryId = cat.id
    LEFT JOIN msg_sub_category subcat ON msg.subCatId = subcat.id
    LEFT JOIN location loc ON msg.locationId = loc.id
    LEFT JOIN locationtype loctp ON loc.locationTypeId = loctp.id
    LEFT JOIN main_area a ON loc.areaId = a.id

    left join point pt on loc.id = pt.locationId
  WHERE
    msg.isLatest = TRUE
    -- and msg.messageId is not null
  ORDER BY msg.id
  LIMIT :limit
  OFFSET :offset
