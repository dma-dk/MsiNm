
insert into User (id, created, updated, version, email, first_name, last_name, password, password_salt) values (0, current_timestamp, current_timestamp, 1, 'a@b.dk', 'Mr', 'User', 'a2605a8c6e4b61d82dedb539f63036c423d5418dabe4c8ba9cbc86986aed0407c712828b907a5380c4e7ada14cc6750d5aae3107210c0d818c3c83c6b82f2fba', 'yy');
insert into User (id, created, updated, version, email, first_name, last_name, password, password_salt) values (-1, current_timestamp, current_timestamp, 1, 'c@d.dk', 'Ms', 'Admin', 'bb56b88807e8a5e053a7b775726752870aee7c1c78754b2648b869cc1aa5bdba1376480fadf8102afb2d828b8a4e3ee4024e723efe0c50ff5d1731299411632d', 'xx');
insert into User (id, created, updated, version, email, first_name, last_name, password, password_salt) values (-2, current_timestamp, current_timestamp, 1, 'mcb@dma.dk', 'Mads', 'Billesø', 'be4a67597af2867f138d24edb7b1dac60aca4c46547d809dc9dd4598457f50a6b1f084f408aedc73f99a2e74eb2724685cf2acc0e9d12e8fee7ae329bf2b8ab5', 'mcb@dma.dk');

insert into msi.Role (id, name) values (0, 'user');
insert into msi.Role (id, name) values (-1, 'admin');

insert into msi.User_Role (user_id, roles_id) values (0,0);
insert into msi.User_Role (user_id, roles_id) values (0,-1);
insert into msi.User_Role (user_id, roles_id) values (-1,0);
insert into msi.User_Role (user_id, roles_id) values (-2,0);
insert into msi.User_Role (user_id, roles_id) values (-2,-1);

