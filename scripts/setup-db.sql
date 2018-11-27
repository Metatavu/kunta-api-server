create user if not exists sa identified by 'sa';
grant all on katest.* to sa; 
drop database if exists katest; 
CREATE DATABASE katest /*!40100 DEFAULT CHARACTER SET utf8mb4 */