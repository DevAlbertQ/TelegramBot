CREATE TABLE `tb_user` (
	`id_user` bigint NOT NULL,
	`full_name` varchar(100),
	`username` varchar(100),
	`add_date` datetime(6),
	`cell_number` varchar(20),
	`email` varchar(100), 
  PRIMARY KEY (`id_user`)
 );