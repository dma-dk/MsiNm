
select
  distinct m.id
from
  Message m
  left join Area a on m.area_id = a.id
where
  a.lineage like :lineage
