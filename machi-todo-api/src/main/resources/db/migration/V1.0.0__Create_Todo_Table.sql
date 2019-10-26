CREATE TABLE todo
(
  id  BIGINT PRIMARY KEY,
  createdby   VARCHAR(100) NOT NULL ,
  description VARCHAR(500),
  ordering    INT,
  status      INT,
  title VARCHAR(255) NOT NULL,
  UNIQUE (title, createdBy)
);

CREATE SEQUENCE TODO_SEQUENCE_ID INCREMENT 1 START 1 OWNED BY todo.id;
