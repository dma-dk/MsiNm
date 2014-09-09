
select
  fa.id             as id,
  fa.active         as active,
  ma.area_danish    as area1_da,
  ma.area_english   as area1_en,
  fa.name_dk        as area2_da,
  fa.name_eng       as area2_en,
  fap.lat_deg       as lat_deg,
  fap.lat_min       as lat_min,
  fap.long_deg      as lon_deg,
  fap.long_min      as lon_min
from
  firing_area fa
  left join firing_area_position fap on fap.firing_area_id = fa.id
  left join main_area ma on fa.main_area_id = ma.id
where
  fa.active = 1
order by fa.id, fap.sort_order;

