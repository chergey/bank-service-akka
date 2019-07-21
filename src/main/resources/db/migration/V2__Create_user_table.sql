CREATE TABLE "user" (
  "id"      BIGINT PRIMARY  KEY,
  "username" VARCHAR(50)  NOT NULL,
  "email"   VARCHAR(50)  NOT NULL,
  "password"   VARCHAR(50)  NOT NULL
);

INSERT INTO  "user" VALUES(1, 'David', 'david2424@gmail.com', '1111');
INSERT INTO  "user" VALUES(2,  'Donald', 'donald3434@gmail.com', '111');