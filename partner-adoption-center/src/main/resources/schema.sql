create table adoption_request (
  id SERIAL primary key,
  animal bigint not null,
  adopter_name varchar(255) not null,
  email varchar(255) not null,
  notes varchar(1000)
);
