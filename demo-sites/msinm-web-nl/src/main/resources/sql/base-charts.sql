insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '2182A', 1043, '', 750000, 'North Sea-South Sheet');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '126', 1468, '', 60000, 'App. to Den Helder');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '1631', 1418, '', 150000, 'DW Routes to Ijmuiden');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

