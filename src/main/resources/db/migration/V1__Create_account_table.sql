CREATE TABLE "accounts" (
  "id"       INT PRIMARY KEY,
  "balance" DECIMAL NOT NULL,
  "name" VARCHAR(50)  NOT NULL
);
INSERT INTO  "accounts" VALUES(1, 1000.0, 'aaaaa');
INSERT INTO  "accounts" VALUES(2, 1000.0, 'aaaaa');