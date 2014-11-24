insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '303', 100, '', 3500000, 'Norskehavet-Jan Mayen');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '325', null, '', 200000, 'Slettnes/Grense-Jacobs');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '201', null, '', 120000, 'Oslofjorden ');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

