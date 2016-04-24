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
    "EMPTY_FREQUENCY" INTEGER NOT NULL, -- the dumping frequency (in minutes)
    "LAST_EMPTY_TIME" TIMESTAMP NOT NULL DEFAULT (now()), -- the last time the trash was emptied
    PRIMARY KEY("ID")
);

CREATE TABLE IF NOT EXISTS "TOKEN"
(
    "TEXT" VARCHAR(100) NOT NULL UNIQUE, -- the text of the token
    "USER_ID" INTEGER, -- the id of the user that generated the token
    "EXPIRATION_DELAY" TIMESTAMP NOT NULL DEFAULT (now() + 10 * INTERVAL '1 minute') -- the expiration delay (in minutes)
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
    
# --- !Downs

DROP TABLE "TOKEN";
DROP TABLE "TRASH";
DROP TABLE "USER";