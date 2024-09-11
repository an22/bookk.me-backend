create table if not exists user_profile (
	id bigint not null AUTO_INCREMENT,
    name varchar(255) not null,
    last_name varchar(255) not null,
    email varchar(255) not null,
    phone varchar(255) not null,
    role int not null,
    primary key (id)
);