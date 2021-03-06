# --- !Ups

CREATE TABLE IF NOT EXISTS "USER"
(
    "ID" SERIAL NOT NULL, -- the id
    "USERNAME" VARCHAR(30) NOT NULL UNIQUE, -- the username
    "HASH" VARCHAR(100) NOT NULL, -- the hashed password
    PRIMARY KEY("ID")
);

CREATE TABLE IF NOT EXISTS "TRASH"
(
    "ID" SERIAL NOT NULL, -- the id
    "USER_ID" INTEGER, -- the id of the user who owns the trash
    "VOLUME" INTEGER NOT NULL, -- the volume (unitary)
    "EMPTY" BOOLEAN NOT NULL DEFAULT FALSE, -- <true> if the trash is empty
    PRIMARY KEY("ID")
);

CREATE TABLE IF NOT EXISTS "TOKEN"
(
    "TEXT" VARCHAR(100) NOT NULL UNIQUE, -- the text of the token
    "USER_ID" INTEGER, -- the id of the user that generated the token
    "EXPIRATION_DELAY" TIMESTAMP NOT NULL DEFAULT (now() + 10 * INTERVAL '1 minute') -- the expiration delay (in minutes)
);

CREATE TABLE IF NOT EXISTS "WASTE_VOLUME"
(
    "USER_ID" INTEGER, -- the id of the user that generated the waste
    "VOLUME" INTEGER NOT NULL, -- the volume of the waste
    "RECORD_DATE" TIMESTAMP NOT NULL DEFAULT (now()) -- the date the row was recorded
);

ALTER TABLE "TRASH"
    ADD FOREIGN KEY ("USER_ID")
    REFERENCES "USER"("ID")
    MATCH SIMPLE
;

ALTER TABLE "TOKEN"
    ADD FOREIGN KEY ("USER_ID")
    REFERENCES "USER"("ID")
    MATCH SIMPLE
;

ALTER TABLE "WASTE_VOLUME"
    ADD FOREIGN KEY ("USER_ID")
    REFERENCES "USER"("ID")
    MATCH SIMPLE
;
    
# --- !Downs

DROP TABLE "WASTE_VOLUME";
DROP TABLE "TOKEN";
DROP TABLE "TRASH";
DROP TABLE "USER";