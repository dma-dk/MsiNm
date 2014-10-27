
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-10,0,NULL);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-13,110.9611821362541,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-29,-9.038817863745905,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-45,-4.038817863745905,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-58,120.9611821362541,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-115,-19.038817863745905,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-219,37.211182136254095,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-232,70.9611821362541,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-588,23.461182136254095,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-1615,-6.538817863745905,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-1686,9.711182136254095,-10);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-8709,20,NULL);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-8712,10,NULL);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-8715,50,NULL);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-8718,30,NULL);
INSERT INTO Area (id, sortOrder, parent_id) VALUES (-9721,40,NULL);
update Area set created = current_timestamp;
update Area set updated = current_timestamp;
update Area set version = 1;

INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-11,'en','Denmark',-10);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-14,'en','Skagerak',-13);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-30,'en','The Sound',-29);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-46,'en','Great Belt',-45);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-59,'en','North Sea',-58);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-116,'en','Baltic',-115);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-220,'en','Kattegat',-219);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-233,'en','Liimfiord',-232);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-589,'en','Little Belt',-588);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-1616,'en','Waters south of Sealand',-1615);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-1687,'en','Waters south of Funen',-1686);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-8710,'en','Norway',-8709);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-8713,'en','Sweden',-8712);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-8716,'en','Germany',-8715);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-8719,'en','UK',-8718);
INSERT INTO AreaDesc (id, lang, name, entity_id) VALUES (-9722,'en','France',-9721);


