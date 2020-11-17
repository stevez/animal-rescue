create table animal (
  id serial primary key,
  name varchar(255) not null,
  rescue_date date,
  avatar_url varchar(255),
  description varchar(1000)
);

create table adoption_request (
  id serial primary key,
  animal bigint not null,
  adopter_name varchar(255) not null,
  email varchar(255) not null,
  notes varchar(1000)
);

ALTER SEQUENCE adoption_request_id_seq RESTART WITH 12
