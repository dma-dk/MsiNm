
insert into Category (id, parent_id) values  (0, null);
insert into Category (id, parent_id) values  (-1, null);
insert into Category (id, parent_id) values  (-2, null);
insert into Category (id, parent_id) values  (-3, 0);
insert into Category (id, parent_id) values  (-4, 0);
update Category set version = 1;

insert into CategoryDesc (id, lang, entity_id, name) values (0,  'en', 0, 'Aids to Navigation');
insert into CategoryDesc (id, lang, entity_id, name) values (-1,  'da', 0, 'Navigationsværktøjer');
insert into CategoryDesc (id, lang, entity_id, name) values (-2,  'en', -1, 'Dangerous Wreck');
insert into CategoryDesc (id, lang, entity_id, name) values (-3,  'da', -1, 'Farligt vrag');
insert into CategoryDesc (id, lang, entity_id, name) values (-4,  'en', -2, 'Unwieldy Tow');
insert into CategoryDesc (id, lang, entity_id, name) values (-5,  'da', -2, 'Uhåndterligt tov');
insert into CategoryDesc (id, lang, entity_id, name) values (-6,  'en', -3, 'Drifting Hazards');
insert into CategoryDesc (id, lang, entity_id, name) values (-7,  'da', -3, 'Drivende farer');
insert into CategoryDesc (id, lang, entity_id, name) values (-8,  'en', -4, 'Buoy');
insert into CategoryDesc (id, lang, entity_id, name) values (-9,  'da', -4, 'Bøje');

