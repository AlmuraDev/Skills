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

create table if not exists "skills_container_palette"
(
  "rec_no"        int        auto_increment,
  "container"  binary(16)    not null,
  constraint "pk_skills_container_palette_container" primary key("container")
);

create table if not exists "skills_block_creation"
(
  "container"     smallint   not null,
  "pos"           bigint     not null,
  "mask"          bigint     not null,
  constraint "pk_skills_block_creation_container_pos" primary key("container", "pos")
);