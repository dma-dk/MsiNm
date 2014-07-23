 SELECT
  msg.id                   AS id,
  msg.messageId            AS messageId,
  msg.draft                AS statusDraft,
  msg.navtexNo             AS navtexNo,
  msg.navwarning           AS description_en,
  msg.localLanguage        AS description_da,
  msg.enctext              AS title,
  msg.validFrom            AS validFrom,
  msg.validTo              AS validTo,
  msg.datetimeCreated      AS created,
  msg.dateTimeUpdated      AS updated,
  msg.dateTimeDeleted      AS deleted,
  msg.version              AS version,

  prio.priority            AS priority,

  cls.class                AS messageType,

  cat.english              AS category1_en,
  cat.danish               AS category1_da,
  subcat.english           AS category2_en,
  subcat.danish            AS category2_da,

  c.country_english        AS area1_en,
  c.country_danish         AS area1_da,
  a.area_english           AS area2_en,
  a.area_danish            AS area2_da,
  loc.subarea              AS area3_en,
  loc.subAreaLocalLanguage AS area3_da,
  loctp.type               AS locationType,

  pt.ptnNo                 AS pointIndex,
  pt.latitude              AS pointLatitude,
  pt.longitude             AS pointLongitude,
  pt.radius                AS pointRadius
FROM
  message msg
  LEFT JOIN priority prio           ON msg.priorityId = prio.id
  LEFT JOIN msg_class cls           ON msg.msgClassId = cls.id
  LEFT JOIN msg_category cat        ON msg.msgCategoryId = cat.id
  LEFT JOIN msg_sub_category subcat ON msg.subCatId = subcat.id
  LEFT JOIN location loc            ON msg.locationId = loc.id
  LEFT JOIN locationtype loctp      ON loc.locationTypeId = loctp.id
  LEFT JOIN main_area a             ON loc.areaId = a.id
  LEFT JOIN country c               ON a.countryId = c.id

  LEFT JOIN point pt                ON loc.id = pt.locationId
WHERE
  msg.id IN (:ids)
