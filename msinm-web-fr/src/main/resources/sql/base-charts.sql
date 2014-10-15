insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-1, '87', 1413, '', 150000, 'Borkum-Neuwerk/Helgoland');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-2, '43', 1358, '', 50000, 'Gabelsflach-Fehmarnsund');
insert into Chart (id, chartNumber, internationalNumber, horizontalDatum, scale, name) values (-3, '60', null, '', 500000, 'Ostsee, Gedser-Akmenrags');
update Chart set version = 1;
update Chart set horizontalDatum = 'WGS84';

