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

create table if not exists user_auth_info_v2(
	id bigint not null AUTO_INCREMENT,
	username varchar(255) not null unique,
    role int not null,
    primary key (id),
    index user_id_ind (user_id),
    index login_ind (login)
);

create table if not exists user_has_auth_credential (
    username not null varchar(255),
    handle not null varbinary(255),
    cred_descriptor_id not null varbinary(255),
    cred_descriptor_type not null varchar(255),
    cred_descriptor_transports not null varchar(255),
    public_key not null varbinary(255),
    signature_count not null bigint,
    discoverable bit,
    backup_eligible bit not null,
    backed_up bit not null,
    attestation_object not null varbinary(255),
    client_data not null varbinary(255),
    primary key (username),
    index handle_ind (handle)
);