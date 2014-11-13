
INSERT INTO CompositeParamType (id, name) VALUES (-2167,'Vessel ID');
INSERT INTO CompositeParamType (id, name) VALUES (-2173,'Wreck Marking');

INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2168,0,1,'Vessel Name',1,'text');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2169,0,0,'Vessel Call Sign',2,'text');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2170,0,0,'VHF Channel',3,'number');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2174,0,1,'AtoN type',1,'Floating AtoN Type');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2175,0,0,'Light characteristics',2,'text');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2176,0,0,'Distance',3,'text');
INSERT INTO TemplateParam (id,list,mandatory,name,sortKey,`type`) VALUES (-2177,0,0,'Bearing',4,'AtoN Bearing');


INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2167,-2168);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2167,-2169);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2167,-2170);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2173,-2174);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2173,-2175);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2173,-2176);
INSERT INTO CompositeParamType_TemplateParam (CompositeParamType_id,parameters_id) VALUES (-2173,-2177);
