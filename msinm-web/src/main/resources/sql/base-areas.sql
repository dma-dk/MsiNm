
insert into msi.area (id, parent_id) values  (-1, null);
insert into msi.area (id, parent_id) values  (-2, null);
insert into msi.area (id, parent_id) values  (-3, -1);
insert into msi.area (id, parent_id) values  (-4, -3);
insert into msi.area (id, parent_id) values  (-5, -3);
insert into msi.area (id, parent_id) values  (-6, -1);
insert into msi.area (id, parent_id) values  (-7, -6);
update area set version = 1;

insert into msi.areadesc (id, lang, entity_id, name) values (-2,  'en', -1, 'Denmark');
insert into msi.areadesc (id, lang, entity_id, name) values (-3,  'da', -1, 'Danmark');
insert into msi.areadesc (id, lang, entity_id, name) values (-4,  'en', -2, 'England');
insert into msi.areadesc (id, lang, entity_id, name) values (-5,  'da', -2, 'England');
insert into msi.areadesc (id, lang, entity_id, name) values (-6,  'en', -3, 'The Baltic Sea');
insert into msi.areadesc (id, lang, entity_id, name) values (-7,  'da', -3, 'Østersøen');
insert into msi.areadesc (id, lang, entity_id, name) values (-8,  'en', -4, 'Gedser S');
insert into msi.areadesc (id, lang, entity_id, name) values (-9,  'da', -4, 'Gedser S');
insert into msi.areadesc (id, lang, entity_id, name) values (-10, 'en', -5, 'Bornholm SW');
insert into msi.areadesc (id, lang, entity_id, name) values (-11, 'da', -5, 'Bornholm SW');
insert into msi.areadesc (id, lang, entity_id, name) values (-12, 'en', -6, 'The Sound');
insert into msi.areadesc (id, lang, entity_id, name) values (-13, 'da', -6, 'Sundet');
insert into msi.areadesc (id, lang, entity_id, name) values (-14, 'en', -7, 'Copenhagen Harbour');
insert into msi.areadesc (id, lang, entity_id, name) values (-15, 'da', -7, 'Københavns Havn');
