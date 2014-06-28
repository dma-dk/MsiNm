


insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (0, current_timestamp, current_timestamp, 1, 'a@b.dk', 'Mr', 'User', 'a2605a8c6e4b61d82dedb539f63036c423d5418dabe4c8ba9cbc86986aed0407c712828b907a5380c4e7ada14cc6750d5aae3107210c0d818c3c83c6b82f2fba', 'yy');
insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (-1, current_timestamp, current_timestamp, 1, 'c@d.dk', 'Ms', 'Admin', 'bb56b88807e8a5e053a7b775726752870aee7c1c78754b2648b869cc1aa5bdba1376480fadf8102afb2d828b8a4e3ee4024e723efe0c50ff5d1731299411632d', 'xx');

insert into msi.role (id, name) values (0, 'user');
insert into msi.role (id, name) values (-1, 'admin');

insert into msi.user_role (user_id, roles_id) values (0,0);
insert into msi.user_role (user_id, roles_id) values (0,-1);
insert into msi.user_role (user_id, roles_id) values (-1,0);

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


insert into msi.category (id, parent_id) values  (0, null);
insert into msi.category (id, parent_id) values  (-1, null);
insert into msi.category (id, parent_id) values  (-2, null);
insert into msi.category (id, parent_id) values  (-3, 0);
insert into msi.category (id, parent_id) values  (-4, 0);
update category set version = 1;

insert into msi.categorydesc (id, lang, entity_id, name) values (0,  'en', 0, 'Aids to Navigation');
insert into msi.categorydesc (id, lang, entity_id, name) values (-1,  'da', 0, 'Navigationsværktøjer');
insert into msi.categorydesc (id, lang, entity_id, name) values (-2,  'en', -1, 'Dangerous Wreck');
insert into msi.categorydesc (id, lang, entity_id, name) values (-3,  'da', -1, 'Farligt vrag');
insert into msi.categorydesc (id, lang, entity_id, name) values (-4,  'en', -2, 'Unwieldy Tow');
insert into msi.categorydesc (id, lang, entity_id, name) values (-5,  'da', -2, 'Uhåndterligt tov');
insert into msi.categorydesc (id, lang, entity_id, name) values (-6,  'en', -3, 'Drifting Hazards');
insert into msi.categorydesc (id, lang, entity_id, name) values (-7,  'da', -3, 'Drivende farer');
insert into msi.categorydesc (id, lang, entity_id, name) values (-8,  'en', -4, 'Buoy');
insert into msi.categorydesc (id, lang, entity_id, name) values (-9,  'da', -4, 'Bøje');


insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (0, '60', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-1, '61', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-2, '92', 1300, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-3, '93', 1044, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-4, '94', 1411, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-5, '95', 1451, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-6, '99', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-7, '100', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-8, '101', 1301, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-9, '102', 1302, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-10, '103', 1303, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-11, '104', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-12, '105', 1450, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-13, '106', 1382, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-14, '107', 1383, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-15, '108', 1448, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-16, '109', 1449, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-17, '110', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-18, '111', 1381, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-19, '112', 1380, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-20, '113', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-21, '114', 1377, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-22, '115', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-23, '116', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-24, '117', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-25, '118', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-26, '122', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-27, '123', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-28, '124', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-29, '127', 1372, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-30, '128', 1379, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-31, '129', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-32, '131', 1331, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-33, '132', 1332, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-34, '133', 1333, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-35, '134', 1334, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-36, '141', 1370, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-37, '142', 1368, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-38, '143', 1369, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-39, '144', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-40, '145', 1371, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-41, '151', 1375, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-42, '152', 1373, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-43, '153', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-44, '154', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-45, '155', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-46, '157', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-47, '158', 1376, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-48, '159', 1374, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-49, '160', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-50, '161', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-51, '162', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-52, '163', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-53, '164', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-54, '171', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-55, '172', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-56, '188', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-57, '189', 1336, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-58, '190', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-59, '195', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-60, '196', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-61, '197', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-62, '198', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-63, 'C', null, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (-64, 'D', null, '');
update msi.chart set version = 1;

create index user_email on msi.user (email);

