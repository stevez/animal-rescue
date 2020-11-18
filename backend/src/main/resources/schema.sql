create table animal (
  id bigint primary key auto_increment,
  name varchar(255) not null,
  rescue_date date,
  avatar_url varchar(255),
  description varchar(1000)
);
