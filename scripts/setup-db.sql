create user if not exists sa identified by 'sa';
grant all on katest.* to sa; 
drop database if exists katest; 
create database katest default charset utf8;