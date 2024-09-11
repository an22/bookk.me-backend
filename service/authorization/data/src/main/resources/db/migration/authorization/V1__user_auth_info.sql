create table if not exists user_auth_info (
	id bigint not null AUTO_INCREMENT,
	user_id int not null unique,
	login varchar(255) not null unique,
    password_hash varchar(255) not null,
    totp_secret varchar(255) not null,
    role int not null,
    primary key (id),
    index user_id_ind (user_id)
);


create table if not exists auth_device (
	id bigint not null AUTO_INCREMENT,
	device_name varchar(255) not null,
	user_auth_id bigint not null,
    refresh_token varchar(2048) not null,
    is_signed_in bit not null,
    primary key (id),
    index user_auth_id_ind (user_auth_id),
    foreign key (user_auth_id)
		references user_auth_info(id)
		on delete cascade
);