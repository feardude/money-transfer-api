create table accounts (
  id int primary key,
  idReal varchar(128) unique not null,
  amount decimal default 0
);

insert into accounts values (1, '1', 1000);
insert into accounts values (2, '2', 1000);