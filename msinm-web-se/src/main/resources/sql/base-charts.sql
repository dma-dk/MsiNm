insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '53', 1206, '', 250000, 'Baltic Sea, s.part');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '2', null, '', 1500000, 'Østersjøn,Radio & Dist');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '6141', null, '', 12500, 'Stockholms hamn ');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

