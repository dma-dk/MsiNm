
select
  distinct m.id
from
  Message m
  left join Message_Category mc on mc.Message_id = m.id
  left join Category c on mc.categories_id = c.id
where
  c.lineage like :lineage
