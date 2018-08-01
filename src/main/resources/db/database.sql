create table if not exists "skills_experience"
(
  "rec_no"     int               auto_increment,
  "created"    timestamp         default current_timestamp not null,
  "modified"   timestamp         default current_timestamp not null,
  "skill"      varchar(200)      not null,
  "container"  binary(16)        not null,
  "holder"     binary(16)        not null,
  "experience" decimal           default 0 not null,
  constraint "pk_skill_experience_skill_type_container_holder" primary key("container", "holder", "skill")
);