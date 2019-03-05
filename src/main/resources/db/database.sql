create table if not exists "skills_experience"
(
  "rec_no"     int           auto_increment,
  "created"    timestamp     default current_timestamp not null,
  "modified"   timestamp     default current_timestamp not null,
  "skill"      varchar(200)  not null,
  "container"  binary(16)    not null,
  "holder"     binary(16)    not null,
  "experience" decimal       default 0 not null,
  constraint "pk_skill_experience_skill_type_container_holder" primary key("container", "holder", "skill")
);

create table if not exists "skills_block_creation_pallete"
(
  "rec_no"     int           auto_increment,
  "id"         varchar(200)  not null,
  "name"       varchar(200)  not null,
  constraint "pk_skills_block_creation_pallete_rec_no" primary key("rec_no"),
  constraint "uc_skills_block_creation_pallete_id" unique("id")
);

create table if not exists "skills_block_creation"
(
  "rec_no"        int        auto_increment,
  "container"     binary(16) not null,
  "pos"           bigint     not null,
  "creation_type" int        not null,
  constraint "pk_skills_block_creation_container_pos" primary key("container", "pos"),
  foreign key ("creation_type") references "skills_block_creation_pallete"("rec_no") on update cascade on delete cascade
);