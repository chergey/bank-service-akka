CREATE TABLE "account" (
  "id"       BIGINT PRIMARY KEY,
  "balance" DECIMAL NOT NULL,
  "name" VARCHAR(50)  NOT NULL
);
INSERT INTO  "account" VALUES(1, 1000.0, '');
INSERT INTO  "account" VALUES(2, 1000.0, '');