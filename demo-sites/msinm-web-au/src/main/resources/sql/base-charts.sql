insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '301', null, '', 300000, 'Booby Island to Archer River');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '4620', null, '', 1500000, 'Percy Isles to Buby Island');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '4604', null, '', 3500000, 'Coral and Solomon Seas');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

