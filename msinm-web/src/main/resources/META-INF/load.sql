


insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (0, current_timestamp, current_timestamp, 1, 'a@b.dk', 'Mr', 'User', '294c8e2d592d8b13de92fd6d8254b33a4f4d816e06ec1c158c164a808a3d8164316908dd2580be11660efd8333d1f0f16b4869cb2fb94a657cfd8e3dddbc9714', 'xx');
insert into user (id, created, updated, version, email, first_name, last_name, password, password_salt) values (1, current_timestamp, current_timestamp, 1, 'c@d.dk', 'Ms', 'Admin', '28586f41c2a84433c3bf6f1558c20ae329b297d2fcd6900b602d545cf3a780095c2575076d5294abbc759a1303da658ea918c0ba3758a07a71073d3762898e34', 'yy');

insert into msi.role (id, name) values (0, 'user');
insert into msi.role (id, name) values (1, 'admin');

insert into msi.user_role (user_id, roles_id) values (0,0);
insert into msi.user_role (user_id, roles_id) values (0,1);
insert into msi.user_role (user_id, roles_id) values (1,0);

