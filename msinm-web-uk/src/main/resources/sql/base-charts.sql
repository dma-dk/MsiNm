insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '1975', null, '', 50000, 'Thames Estuary,n.part');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '2', 160, '', 1500000, 'The British Isles');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '266', null, '', 200000, 'North Sea Offshore sh.11');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

