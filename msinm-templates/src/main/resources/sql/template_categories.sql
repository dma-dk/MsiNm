
select
  distinct t.name
from
  Template t
    left join Template_Category tc on tc.Template_id = t.id
    left join Category c on tc.categories_id = c.id
    left join Category msgcat on msgcat.id in (:categoryIds)
where
  (t.type is null or t.type in (:types))
  and msgcat.lineage like concat(c.lineage, '%')
order by lower(t.name);
