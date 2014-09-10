
select
  fp.id             as id,
  fp.t_from         as valid_from,
  fp.t_to           as valid_to,
  fa.id             as area_id,
  fa.name_dk        as area_da,
  i.description_dk  as description_da,
  i.description_eng as description_en,
  i.info_type_id    as info_type
from
    firing_period fp
    left join firing_area fa on fp.f_area_id = fa.id
    left join firing_area_information fai on fai.firing_area_id = fa.id
    left join information i on i.id = fai.information_id
where
  date(fp.t_from) >= CURRENT_DATE and date(fp.t_from) <= CURRENT_DATE + 1
  and fp.t_to > CURRENT_TIME
order by fp.id, i.info_type_id;
