


insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (0, current_timestamp, current_timestamp, 1, 'a@b.dk', 'Mr', 'User', 'a2605a8c6e4b61d82dedb539f63036c423d5418dabe4c8ba9cbc86986aed0407c712828b907a5380c4e7ada14cc6750d5aae3107210c0d818c3c83c6b82f2fba', 'yy');
insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (1, current_timestamp, current_timestamp, 1, 'c@d.dk', 'Ms', 'Admin', 'bb56b88807e8a5e053a7b775726752870aee7c1c78754b2648b869cc1aa5bdba1376480fadf8102afb2d828b8a4e3ee4024e723efe0c50ff5d1731299411632d', 'xx');

insert into msi.role (id, name) values (0, 'user');
insert into msi.role (id, name) values (1, 'admin');

insert into msi.user_role (user_id, roles_id) values (0,0);
insert into msi.user_role (user_id, roles_id) values (0,1);
insert into msi.user_role (user_id, roles_id) values (1,0);

insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (0, 'World', 'Jorden', null);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (1, 'Denmark', 'Danmark', 0);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (2, 'England', 'England', 0);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (3, 'The Baltic Sea', 'Østersøen', 1);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (4, 'Gedser S', 'Gedser S', 3);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (5, 'Bornholm SW', 'Bornholm SW', 3);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (6, 'Sundet', 'The Sound', 1);
insert into msi.area (id, nameEnglish, nameLocal, parentArea_id) values  (7, 'Copenhagen Harbour', 'Københavns Havn', 6);
update area set version = 1;

insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (0, 189, 1336, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (1, 131, 1331, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (2, 134, 1334, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (3, 142, 1368, '');
insert into msi.chart (id, chartNumber, internationalNumber, horizontalDatum) values (4, 152, 1373, '');
update msi.chart set version = 1;

create index user_email on msi.user (email);

